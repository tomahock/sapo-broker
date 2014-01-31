#include <assert.h>
#include <time.h>
#include <string.h>

#include "sapo-broker2.h"
#include "broker_internals.h"
#include "net.h"
#include "protobuf.h"
#include "broker.pb-c.h"

static destination_type_t
atom_2_destination_type ( SapoBroker__Atom__Action__ActionType atom_destination_type ){
    switch( atom_destination_type ){
        case SAPO_BROKER__ATOM__DESTINATION_TYPE__TOPIC:
            return SB_TOPIC;
        case SAPO_BROKER__ATOM__DESTINATION_TYPE__VIRTUAL_QUEUE:
            return SB_VIRTUAL_QUEUE;
        default:
            return SB_QUEUE;
    }
}

static SapoBroker__Atom__Action__ActionType
destination_type_2_atom ( destination_type_t destination_type ){
    switch( destination_type ){
        case SB_TOPIC:
            return SAPO_BROKER__ATOM__DESTINATION_TYPE__TOPIC;
        case SB_VIRTUAL_QUEUE:
            return SAPO_BROKER__ATOM__DESTINATION_TYPE__VIRTUAL_QUEUE;
        default:
            return SAPO_BROKER__ATOM__DESTINATION_TYPE__QUEUE;
    }
}

int
proto_protobuf_send(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest,
        broker_sendmsg_t *sendmsg)
{
    int rc = 0;
    char *msg;
    size_t len;

    ProtobufCBinaryData             payload;
    SapoBroker__Atom                atom    = SAPO_BROKER__ATOM__INIT;
    SapoBroker__Atom__Action        action  = SAPO_BROKER__ATOM__ACTION__INIT;
    SapoBroker__Atom__Publish       publish = SAPO_BROKER__ATOM__PUBLISH__INIT;
    SapoBroker__Atom__BrokerMessage message = SAPO_BROKER__ATOM__BROKER_MESSAGE__INIT;

    payload.data = (uint8_t *) sendmsg->payload;
    payload.len  = sendmsg->payload_size;

    message.payload  = payload;

    message.message_id = sendmsg->message_id;

    if( 0 == sendmsg->timestamp ) {
        message.has_timestamp = 0;
    } else {
        message.has_timestamp = 1;
        message.timestamp = sendmsg->timestamp;
    }

    if( 0 == sendmsg->expiration ) {
        message.has_expiration = 0;
    } else {
        message.has_expiration = 1;
        message.expiration = sendmsg->expiration;
    }

    publish.message = &message;
    publish.destination = dest->name;
    publish.destination_type = destination_type_2_atom(dest->type);
    publish.action_id = NULL;

    action.publish = &publish;
    action.action_type = SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__PUBLISH;

    atom.action = &action;

    len = sapo_broker__atom__get_packed_size(&atom);
    msg = (char *) malloc( len );
    sapo_broker__atom__pack(&atom, (uint8_t *) msg);

    rc = net_send(sb, srv, msg, len);
    free(msg);

    return rc;
}


int
proto_protobuf_subscribe(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest)
{
    int rc = 0;
    char *msg;
    size_t len;

    SapoBroker__Atom                atom      = SAPO_BROKER__ATOM__INIT;
    SapoBroker__Atom__Action        action    = SAPO_BROKER__ATOM__ACTION__INIT;
    SapoBroker__Atom__Subscribe     subscribe = SAPO_BROKER__ATOM__SUBSCRIBE__INIT;

    subscribe.destination = dest->name;
    subscribe.destination_type = destination_type_2_atom(dest->type);

    action.subscribe = &subscribe;
    action.action_type = SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__SUBSCRIBE;

    atom.action = &action;

    len = sapo_broker__atom__get_packed_size(&atom);
    msg = (char *) malloc( len );
    sapo_broker__atom__pack(&atom, (uint8_t *) msg);

    rc = net_send(sb, srv, msg, len);
    free(msg);

    return rc;
}

broker_msg_t *
proto_protobuf_read_msg( sapo_broker_t *sb, _broker_server_t *srv)
{
    int rc = 0;
    char *buf;
    int len;
    broker_msg_t *msg;
    SapoBroker__Atom                *atom;
    SapoBroker__Atom__Notification  *notification;
    SapoBroker__Atom__BrokerMessage *message; 

    log_debug(sb, "protobuf_read_msg(), reading from network");

    buf = net_recv( sb, srv, &len);
    if( NULL == buf ) {
        log_err(sb, "protobuf_read_msg(): failed reading msg from network.");
        return NULL;
    }

    atom = sapo_broker__atom__unpack(NULL, len, (uint8_t *) buf);

    /* FIXME: handle other message types properly */
    if( atom->action->action_type != SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__NOTIFICATION) {
        log_info(sb, "protobuf_read_msg(): msg is not NOTIFICATION, NOT IMPLEMENTED YET.");
        return NULL;
    }

    /* FIXME check for allocation error */
    msg = (broker_msg_t *) calloc( 1, sizeof(broker_msg_t));
    msg->server = srv->srv;

    notification = atom->action->notification;

    msg->origin = broker_get_destination(
        sb,
        notification->subscription,
        atom_2_destination_type( notification->destination_type )
    );

    message = notification->message;
    if( (msg->origin.type == SB_QUEUE || msg->origin.type == SB_VIRTUAL_QUEUE ) && msg->origin.queue_autoack ) {
        rc = proto_protobuf_send_ack( sb, srv, msg->origin.name, message->message_id );
        if( rc == SB_ERROR )
            msg->acked = 0;
        else
            msg->acked = 1;
    }

    msg->payload_len = message->payload.len;
    msg->payload = (char *) malloc( msg->payload_len  + 1 );
    assert(msg->payload);


    /* FIXME: too many data copies */
    memcpy(msg->payload, message->payload.data, msg->payload_len );
    msg->payload[msg->payload_len] = '\0';

    msg->message_id = strdup( message->message_id );
    assert(msg->message_id);

    log_info(sb, "protobuf_read_msg: from: %s, size: %u", msg->origin.name, (uint_t) msg->payload_len);
    return msg;
}


int
proto_protobuf_send_ack( sapo_broker_t *sb, _broker_server_t *srv, char *dest_name, char *message_id )
{
    int rc = 0;
    size_t len;
    char *msg;

    SapoBroker__Atom                        atom    = SAPO_BROKER__ATOM__INIT;
    SapoBroker__Atom__Action                action  = SAPO_BROKER__ATOM__ACTION__INIT;
    SapoBroker__Atom__AcknowledgeMessage    ack     = SAPO_BROKER__ATOM__ACKNOWLEDGE_MESSAGE__INIT;

    ack.destination = dest_name;
    ack.message_id = message_id;

    action.ack_message = &ack;
    action.action_type = SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__ACKNOWLEDGE_MESSAGE;

    atom.action = &action;

    len = sapo_broker__atom__get_packed_size(&atom);
    msg = (char *) malloc( len );
    sapo_broker__atom__pack(&atom, (uint8_t *) msg);

    rc = net_send(sb, srv, msg, len);
    free(msg);

    return rc;
}
