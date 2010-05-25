#ifndef _CONN_H
#define _CONN_H

#include <sys/time.h>   // struct timeval
#include <sys/types.h>  // NOSIGPIPE
#include <sys/socket.h> // NOSIGPIPE

#include "sapo-broker2.h"


/* NO SIGPIPE on net write error please */
#ifdef MSG_NOSIGNAL
    #define SB_MSG_NOSIGPIPE MSG_NOSIGNAL
#elif MSG_NOSIGPIPE
    #define SB_MSG_NOSIGPIPE MSG_NOSIGPIPE
#else
    #define SB_MSG_NOSIGPIPE 0
#endif



#define SB_NET_HEADER_SIZ   4 /* 2 shorts + 1 int */

_broker_server_t *
_broker_server_get_active( sapo_broker_t *sb );

int
_broker_server_disconnect_all(sapo_broker_t *sb);

int
_broker_server_connect_all(sapo_broker_t *sb);

int
_broker_server_set_faulty( sapo_broker_t *sb, broker_server_t *server);

int
net_send(sapo_broker_t *sb, _broker_server_t *srv, const char *buf, size_t len);

/* wait for incomming msg from any server */
_broker_server_t *
net_poll( sapo_broker_t *sb, struct timeval *tv);

char * net_recv( sapo_broker_t *sb, _broker_server_t *srv, int *buf_len);


#endif // _CONN_H
