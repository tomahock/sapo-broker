#ifndef _CONN_H
#define _CONN_H

#include <sapo_broker.h>

_broker_server_t *
_broker_server_get_active( sapo_broker_t *sb );

int
_broker_server_set_faulty( sapo_broker_t *sb, broker_server_t *server);

int
net_send(sapo_broker_t *sb, _broker_server_t *srv, const char *buf, size_t len);

/* wait for incomming msg from any server */
_broker_server_t *
net_poll( sapo_broker_t *sb, struct timeval *tv);

char * net_recv( sapo_broker_t *sb, _broker_server_t *srv, int *buf_len);


#endif // _CONN_H
