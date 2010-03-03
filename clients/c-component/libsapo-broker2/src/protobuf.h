#ifndef _PROTO_H_
#define _PROTO_H_

#ifdef __cplusplus
extern "C" {
#endif
#include "broker_internals.h"

#ifdef __cplusplus
} /* closing brace for extern "C" */
#endif


/* PROTOCOL BUFFER IS IN C++ */

#ifdef __cplusplus
extern "C" {
#endif

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

int proto_protobuf_send_ack( sapo_broker_t *sb, _broker_server_t *srv, const char * dest_name, const char * message_id );

#ifdef __cplusplus
} /* closing brace for extern "C" */
#endif

#endif // PROTO_H_

