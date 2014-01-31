#include "sapo-broker2.h"
#include "broker_internals.h"
#include "net.h"


int
proto_thrift_send(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest,
        broker_sendmsg_t *sendmsg)
{
    log_err( sb, "thrift_send(): NOT IMPLEMENTED.");
    return SB_ERROR;
}


int
proto_thrift_subscribe(
        sapo_broker_t *sb,
        _broker_server_t *srv,
        broker_destination_t *dest)
{
    log_err( sb, "thrift_subscribe(): NOT IMPLEMENTED.");
    return SB_ERROR;
}

broker_msg_t *
proto_thrift_read_msg( sapo_broker_t *sb, _broker_server_t *srv)
{
    log_err( sb, "thrift_read_msg(): NOT IMPLEMENTED.");
    return NULL;
}
int proto_thrift_send_ack( sapo_broker_t *sb, _broker_server_t *srv, const char * dest_name, const char * message_id )
{
    log_err( sb, "thrift_send_ack(): NOT IMPLEMENTED.");
    return SB_ERROR;
}

