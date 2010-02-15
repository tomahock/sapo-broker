#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#include "sapo_broker.h"
#include "broker_internals.h"
#include "protocol.h"
#include "net.h"


/* private members */
static char _global_broker_err_msg[SB_BUFSIZ];
static int broker_add_destination(sapo_broker_t *sb, broker_destination_t dest);

static _broker_server_t *
broker_get_server_from_msg(sapo_broker_t *sb, broker_msg_t *msg)
{
    uint_t i;
    _broker_server_t *srv = NULL;
    int host_len = strlen(msg->server.hostname);

    for(i = 0; i < sb->servers.server_count; i++) {
        srv = &(sb->servers.server[i]);
        if( srv->srv.port == msg->server.port &&
             !strncmp(srv->srv.hostname, msg->server.hostname, host_len) )
            return srv;
    }
    return srv;
}



sapo_broker_t *
broker_init( broker_server_t server_1)
{
    sapo_broker_t * sb = NULL;

    sb = (sapo_broker_t *) calloc( 1, sizeof(sapo_broker_t) );
    if( !sb ) {
        snprintf(_global_broker_err_msg, SB_BUFSIZ,
                "broker_init() failed to alocate memory.");
        return NULL;
    }

    sb->servers.server = calloc(2, sizeof(broker_server_t));
    sb->servers.array_count = 2;
    sb->servers.server_count = 1;
    memcpy(sb->servers.server, &server_1, sizeof(broker_server_t));

    sb->destinations.dest = calloc(2, sizeof(broker_destination_t) );
    sb->destinations.array_count = 2;
    sb->destinations.dest_count = 0;

    if( server_1.protocol > THRIFT ) {
        server_1.protocol = PROTOBUF;
        log_info( sb, "broker_init(): bad protocol, defaulting to PROTOCOL BUFFERS");
    }

    return sb;
}

int
broker_send( sapo_broker_t *sb,
             broker_destination_t dest,
             char *msg,
             size_t size)
{
    int rc = 0;
    _broker_server_t *srv = NULL;

    for(int i=0; i < 3; i++) {
        srv = _broker_server_get_active( sb );
        switch ( srv->srv.protocol ) {
            case SOAP: {
                           rc = proto_soap_send( sb, srv, &dest, msg, size );
                           break;
                       };
            case PROTOBUF: {
                               rc = proto_protobuf_send( sb, srv, &dest, msg, size);
                               break;
                           };
            case THRIFT: {
                             rc = proto_thrift_send ( sb, srv, &dest, msg, size);
                             break;
                         };
            default: {
                         rc = SB_ERROR;
                         log_err(sb, "invalid server protocol.");
                         break;
                     };
        }
        if( rc != SB_OK ) {
            log_err(sb, "broker_send: failed, trying again.");
            continue;
        }
        break;
    }
    if( rc != SB_OK ) {
        log_err(sb, "broker_send(): failed multiple times sending to server");
    }

    return rc;
}

int
broker_publish( sapo_broker_t *sb,
                char *topic,
                char *msg,
                size_t size)
{
    broker_destination_t dest;

    dest.name = topic;
    dest.type = SB_TOPIC;
    return broker_send(sb, dest, msg, size);
}

int
broker_enqueue( sapo_broker_t *sb,
                char *queue,
                char *msg,
                size_t size)
{
    broker_destination_t dest;

    dest.name = queue;
    dest.type = SB_QUEUE;
    return broker_send(sb, dest, msg, size);
}


int
broker_subscribe( sapo_broker_t *sb, broker_destination_t dest)
{
    int rc = 0;
    _broker_server_t *srv = NULL;

    for(int i=0; i < 3; i++) {
        srv = _broker_server_get_active( sb );
        switch ( srv->srv.protocol ) {
            case SOAP:
                rc = proto_soap_subscribe( sb, srv, &dest );
                break;

            case PROTOBUF:
                rc = proto_protobuf_subscribe( sb, srv, &dest );
                break;

            case THRIFT:
                rc = proto_thrift_subscribe( sb, srv, &dest );
                break;

            default:
                rc = SB_ERROR;
                log_err(sb, "broker_subscribe: invalid server protocol.");
                break;
        }
        if( rc != SB_OK ) {
            log_err(sb, "broker_subscribe: failed, trying again.");
            continue;
        }
        break;
    }
    if( rc != SB_OK ) {
        log_err(sb, "broker_subscribe(): failed multiple times talking to server");
    } else {

        broker_add_destination(sb, dest);
    }

    return rc;
}


int
broker_subscribe_topic( sapo_broker_t *sb, char *topic)
{
    broker_destination_t dest;

    dest.name = topic;
    dest.type = SB_TOPIC;
    dest.queue_autoack = 0;

    return broker_subscribe(sb, dest);
}

int
broker_subscribe_queue( sapo_broker_t *sb, char *queue, bool queue_autoack)
{
    broker_destination_t dest;

    dest.name = queue;
    dest.type = SB_QUEUE;
    dest.queue_autoack = queue_autoack;

    return broker_subscribe(sb, dest);
}


int
broker_msg_free( broker_msg_t *msg)
{
    if( !msg )
        return 0;
    if( msg->payload )
        free(msg->payload);
    if( msg->message_id )
        free(msg->message_id);
    if( msg )
        free(msg);

    return 0;
}

int
broker_msg_ack( sapo_broker_t *sb, broker_msg_t *msg)
{
    int rc;
    if( !msg || !msg->message_id) {
        log_err(sb, "broker_msg_ack(): bad message, cannot acknowledge");
        return SB_ERROR;
    }

    _broker_server_t *srv = broker_get_server_from_msg(sb, msg);
    switch ( msg->server.protocol ) {
        case SOAP:
            rc = proto_soap_send_ack( sb, srv, msg->origin.name, msg->message_id);
            break;
        case PROTOBUF:
            rc = proto_protobuf_send_ack( sb, srv, msg->origin.name, msg->message_id);
            break;
        case THRIFT:
            rc = proto_thrift_send_ack( sb, srv, msg->origin.name, msg->message_id);
            break;
        default:
            rc = SB_ERROR;
            log_err(sb, "broker_msg_ack: invalid server in msg.");
            break;
    }

    broker_msg_free(msg);
    return rc;
}

broker_msg_t *
broker_receive( sapo_broker_t *sb, struct timeval *timeout)
{
    broker_msg_t *msg;
    _broker_server_t *srv = NULL;
    int rc = 0;

    srv = net_poll( sb, timeout);
    if( srv == NULL ) {
        log_debug(sb, "receive(): net_poll() -> NULL");
        return NULL;
    }

    log_debug(sb, "receive(): data from: %s:%d",
                srv->srv.hostname, srv->srv.port);
    switch ( srv->srv.protocol ) {
        case SOAP:
            msg = proto_soap_read_msg( sb, srv );
            break;

        case PROTOBUF:
            msg = proto_protobuf_read_msg( sb, srv );
            break;

        case THRIFT:
            msg = proto_thrift_read_msg( sb, srv );
            break;

        default:
            rc = SB_ERROR;
            log_err(sb, "broker_receive: invalid server protocol in received packet.");
    }
    if( rc != SB_OK ) {
        log_err(sb, "broker_receive: failed reading message.");
        return NULL;
    }

    return msg;
}


int
broker_destroy(sapo_broker_t *sb)
{
    /* close all open connections to servers */
    /* deallocate all memory */
   log_err( sb, "broker_destroy(): NOT IMPLEMENTED.");

   return 1;
}


char *
broker_error( sapo_broker_t *sb)
{
    if( !sb )
        return _global_broker_err_msg;
    return sb->last_error_msg;
}

static int
broker_add_destination(sapo_broker_t *sb, broker_destination_t dest)
{
    int rc = 0;
    /* any space left on destinations array ? */
    if( sb->destinations.dest_count + 1 == sb->destinations.array_count ) {
        uint_t new_size = sizeof(broker_destination_t) * sb->destinations.array_count * 2;
        broker_destination_t *dst = realloc( sb->destinations.dest, new_size );
        if( dst == NULL ) {
            log_err(sb, "broker_subscribe(): failed to alocate memory, can't remember destination");
            return rc; // maybe everything will work just fine...
        }
        sb->destinations.dest = dst;
        sb->destinations.array_count *= 2;
    }

    sb->destinations.dest[ sb->destinations.dest_count++ ] = dest;
    return rc;
}

/* saved destinations have auto_ack settings that should be respected, gotta fetch it */
broker_destination_t
broker_get_destination( sapo_broker_t *sb, const char *dest_name, uint8_t type)
{
    broker_destination_t *dest;
    for(uint_t i = 0; i < sb->destinations.dest_count; i++) {
        dest = &(sb->destinations.dest[i]);
        if( dest->type == type && !strcmp(dest->name, dest_name) )
            return *dest;
    }
    /* not found? .. log error and return destination without auto-ack */
    broker_destination_t dst = { dest_name, type, 0 };
    log_err(sb, "broker_get_destination: cannot find this destination on destination list.");

    return dst;
}


