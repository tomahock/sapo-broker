#ifndef _THRIFT_H_
#define _THRIFT_H_

int
proto_thrift_send(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest,
        broker_sendmsg_t *sendmsg);

int
proto_thrift_subscribe(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest);

broker_msg_t *
proto_thrift_read_msg( sapo_broker_t *sb, _broker_server_t *srv );

int
proto_thrift_send_ack( sapo_broker_t *sb, _broker_server_t *srv, const char * dest_name, const char * message_id );

#endif // _THRIFT_H_
