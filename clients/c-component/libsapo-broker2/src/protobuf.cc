#ifdef __cplusplus
extern "C" {
#endif

#include <assert.h>
#include <time.h>

#include "sapo-broker2.h"
#include "broker_internals.h"
#include "net.h"
#include "protobuf.h"

#ifdef __cplusplus
} /* closing brace for extern "C" */
#endif
#include "broker.pb.h"


static destination_type_t
atom_2_destination_type ( sapo_broker::Atom_DestinationType atom_destination_type ){
    switch( atom_destination_type ){
        case sapo_broker::Atom_DestinationType_TOPIC:
            return SB_TOPIC;
        case sapo_broker::Atom_DestinationType_VIRTUAL_QUEUE:
            return SB_VIRTUAL_QUEUE;
        default:
            return SB_QUEUE;

    }
}

static sapo_broker::Atom_DestinationType
destination_type_2_atom ( destination_type_t destination_type ){
    switch( destination_type ){
		case SB_TOPIC:
            return sapo_broker::Atom_DestinationType_TOPIC;
		case SB_VIRTUAL_QUEUE:
            return sapo_broker::Atom_DestinationType_VIRTUAL_QUEUE;
        default:
            return sapo_broker::Atom_DestinationType_QUEUE;
    }
}



extern "C"
int
proto_protobuf_send(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest,
        broker_sendmsg_t *sendmsg)
{
    int rc = 0;
    std::string msg;
    sapo_broker::Atom *atom;
    sapo_broker::Atom::Action *action;
    sapo_broker::Atom::Publish *publish;
    sapo_broker::Atom_BrokerMessage *message;

    atom = new sapo_broker::Atom();
    action = atom->mutable_action();
    action->set_action_type( sapo_broker::Atom_Action_ActionType_PUBLISH );

    publish = action->mutable_publish();
    publish->set_destination( dest->name );
    publish->set_destination_type( destination_type_2_atom( static_cast<destination_type_t> ( dest->type ) ) );

    message = publish->mutable_message();

    /* setting fields */
    message->set_payload( sendmsg->payload, sendmsg->payload_size );

    if( sendmsg->timestamp != 0)
        message->set_timestamp( sendmsg->timestamp );
    else
        message->set_timestamp( time(NULL) );

    if( sendmsg->message_id != NULL)
        message->set_message_id( sendmsg->message_id );

    if( sendmsg->expiration != 0 )
        message->set_expiration( sendmsg->expiration );


    atom->SerializeToString( &msg );

    rc = net_send(sb, srv, msg.c_str(), msg.size());
    delete atom;

    return rc;
}


extern "C"
int
proto_protobuf_subscribe(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest)
{
    int rc = 0;
    std::string msg;
    sapo_broker::Atom *atom;
    sapo_broker::Atom::Action *action;
    sapo_broker::Atom::Subscribe *subscribe;

    atom = new sapo_broker::Atom();
    action = atom->mutable_action();
    action->set_action_type( sapo_broker::Atom_Action_ActionType_SUBSCRIBE );

    subscribe = action->mutable_subscribe();
    subscribe->set_destination( dest->name );
    if( dest->type == SB_TOPIC )
        subscribe->set_destination_type( sapo_broker::Atom_DestinationType_TOPIC );
    else
        subscribe->set_destination_type( sapo_broker::Atom_DestinationType_QUEUE );

    atom->SerializeToString( &msg );

    rc = net_send(sb, srv, msg.c_str(), msg.size());
    delete atom;

    return rc;
}

extern "C"
broker_msg_t *
proto_protobuf_read_msg( sapo_broker_t *sb, _broker_server_t *srv)
{
    int rc = 0;
    char *buf;
    int buf_len = 0;
    broker_msg_t *msg;
    std::string str;
    sapo_broker::Atom atom;
    sapo_broker::Atom::Action action;
    sapo_broker::Atom::Notification notification;
    sapo_broker::Atom::BrokerMessage message;


    log_debug(sb, "protobuf_read_msg(), reading from network");
    buf = net_recv( sb, srv, &buf_len);
    if( NULL == buf ) {
        log_err(sb, "protobuf_read_msg(): failed reading msg from network.");
        return NULL;
    }


    /* FIXME: too many data copies */
    atom.ParseFromString( std::string( buf, buf_len ) );
    /* free buf alloced by net_recv */
    free(buf);


    /* FIXME: handle other message types properly */
    if( atom.action().action_type() != sapo_broker::Atom_Action_ActionType_NOTIFICATION) {
        log_info(sb, "protobuf_read_msg(): msg is not NOTIFICATION, NOT IMPLEMENTED YET.");
        return NULL;
    }

    msg = (broker_msg_t *) calloc( 1, sizeof(broker_msg_t));
    msg->server = srv->srv;

    notification = atom.action().notification();

    msg->origin = broker_get_destination(
        sb,
        notification.destination().c_str(),
        atom_2_destination_type( notification.destination_type() )
    );

    message = notification.message();
    if( (msg->origin.type == SB_QUEUE || msg->origin.type == SB_VIRTUAL_QUEUE ) && msg->origin.queue_autoack ) {
        rc = proto_protobuf_send_ack( sb, srv, msg->origin.name, message.message_id().c_str() );
        if( rc == SB_ERROR )
            msg->acked = 0;
        else
            msg->acked = 1;
    }

    msg->payload_len = message.payload().size();
    msg->payload = (char *) malloc( msg->payload_len  +1 );
    assert(msg->payload);

    uint_t id_len = message.message_id().size();
    msg->message_id = (char *) malloc( id_len + 1);
    assert(msg->message_id);

    /* FIXME: too many data copies */
    memcpy(msg->payload, message.payload().c_str(), msg->payload_len );
    msg->payload[msg->payload_len] = '\0';

    memcpy(msg->message_id, message.message_id().c_str(), id_len);
    msg->message_id[id_len] = '\0';

    log_info(sb, "protobuf_read_msg: from: %s, size: %u", msg->origin.name, (uint_t) msg->payload_len);
    return msg;
}


int
proto_protobuf_send_ack( sapo_broker_t *sb, _broker_server_t *srv, const char *dest_name, const char *message_id )
{
    int rc = 0;
    std::string msg;
    sapo_broker::Atom *atom;
    sapo_broker::Atom::Action *action;
    sapo_broker::Atom::AcknowledgeMessage *acknowledge;

    atom = new sapo_broker::Atom();
    action = atom->mutable_action();
    action->set_action_type( sapo_broker::Atom_Action_ActionType_ACKNOWLEDGE_MESSAGE );

    acknowledge = action->mutable_ack_message();
    acknowledge->set_destination( dest_name );
    acknowledge->set_message_id( message_id );

    atom->SerializeToString( &msg );

    rc = net_send(sb, srv, msg.c_str(), msg.size());
    delete atom;

    return rc;
}
