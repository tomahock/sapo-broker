#ifndef _PROTO_H_
#define _PROTO_H_

#include "broker_internals.h"

int
proto_protobuf_send(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest,
        broker_sendmsg_t *sendmsg);

int
proto_protobuf_subscribe(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest);

broker_msg_t *
proto_protobuf_read_msg( sapo_broker_t *sb, _broker_server_t *srv );

int proto_protobuf_send_ack( sapo_broker_t *sb, _broker_server_t *srv, char * dest_name, char * message_id );

#endif // PROTO_H_
