#include <assert.h>
#include <errno.h>
#include <netdb.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>


#include "sapo_broker.h"
#include "broker_internals.h"
#include "net.h"



/* private function declarations */
static int
_server_connect( sapo_broker_t *sb, _broker_server_t *server );
static int
_net_send(sapo_broker_t *sb, int fd, const char *buf, size_t len, int flags);


_broker_server_t *
_broker_server_get_active( sapo_broker_t *sb)
{
    uint_t i;
    _broker_server_t *srv = NULL;

    if( NULL == sb ||
        NULL == sb->servers.server )
        return NULL;

    srv = sb->servers.server;

    for(i= 0; i < sb->servers.server_count; i++) {
        if ( srv[i].connected )
            return srv;
    }

    for(i= 0; i < sb->servers.server_count; i++) {
        if ( ! _server_connect( sb, &srv[i] ) )
            log_info(sb, "connected to: %s:%d", srv[i].srv.hostname, srv[i].srv.port);
            return srv;
    }

    log_err(sb, "server_get_active(): couldn't connect to any of the servers.");
    return NULL;
}

int
_broker_server_connect_all(sapo_broker_t *sb)
{
    uint_t i;
    _broker_server_t *srv = NULL;

    if( NULL == sb ||
        NULL == sb->servers.server )
        return SB_NOT_CONNECTED;

    srv = sb->servers.server;
    for(i = 0; i < sb->servers.server_count; i++) {
        if( ! srv[i].connected ) {
            _server_connect(sb, &srv[i]);
        }
    }

    return SB_OK;
}

int
_broker_server_disconnect_all(sapo_broker_t *sb)
{
    uint_t i;
    _broker_server_t *srv = NULL;

    if( NULL == sb ||
        NULL == sb->servers.server )
        return SB_NOT_CONNECTED;

    srv = sb->servers.server;
    for(i = 0; i < sb->servers.server_count; i++) {
        if( srv[i].connected ) {
            close(srv[i].fd); // best effort only.
            srv[i].fail_count = 0;
            srv[i].connected = 0;
        }
    }

    return SB_OK;
}





static int
_server_connect( sapo_broker_t *sb, _broker_server_t *server )
{
    struct hostent *he = NULL;
    struct sockaddr_in server_addr;
    int rc = 0;

    if (server && server->connected) {
        return SB_OK;
    }

    log_info(sb, "");
    log_debug(sb, "connect(): trying %s:%d", server->srv.hostname , server->srv.port);

    /* FIXME: do this sooner and only once on server_add */
    if( server->srv.transport == TCP )
        server->socket_type = SOCK_STREAM;
    else
        server->socket_type = SOCK_DGRAM;

    rc = socket(AF_INET, server->socket_type, 0);
    if ( rc < 0) {
        log_err(sb, "connect(): %s", strerror(errno));
        return SB_ERROR;
    }
    server->fd = rc;

    he = gethostbyname( server->srv.hostname );
    if ( he == NULL) {
        server->connected = FALSE;
        server->fail_count++;
        log_err(sb, "gethostbyhostname: %s", strerror(errno));
        return SB_ERROR;
    }

    memset(&server_addr, 0, sizeof(server_addr));
    memcpy(&server_addr.sin_addr, he->h_addr_list[0], he->h_length);
    server_addr.sin_family = AF_INET;   // Internet address family
    server_addr.sin_port = htons(server->srv.port);   // Server port

    rc = connect(server->fd, (struct sockaddr *) &server_addr,
          sizeof(server_addr));
    if ( rc < 0) {
        server->fail_count++;
        log_err(sb, "connect(): %s", strerror(errno));
        close(server->fd);
        server->connected = FALSE;
        return SB_ERROR;
    }
    server->fail_count = 0;
    server->connected = 1;

    return SB_OK;
}

static int
_net_send(sapo_broker_t *sb, int socket, const char *buf, size_t len, int flags)
{
    size_t remain = len;
    int rc = 0;
    do {
        rc = send(socket, buf, remain, flags);
        if (rc < 0 && errno == EINTR) {
            continue;
        }

        if (errno == ENOTCONN) {
            log_err(sb, "send(): Error writing (Not connected): %s",
                    strerror(errno));
            return SB_NOT_CONNECTED;
        }
        if (rc < 0) {          // got an error writing . Aborting
            log_err(sb, "send(): Erro writing (Unknown): %s", strerror(errno));
            return SB_ERROR_UNKNOWN;
        }
        remain -= rc;
        buf += rc;
    } while (remain > 0);
    log_debug(sb, "send() %d bytes", rc);
    return SB_OK;
}

static int
_net_recv(sapo_broker_t *sb, int socket, char *buf, size_t len, int flags)
{
    size_t remain = len;
    int rc = 0;
    do {
        rc = recv(socket, buf, remain, flags);
        if (rc < 0 && errno == EINTR) {
            continue;
        }

        if (errno == ENOTCONN) {
            log_err(sb, "recv(): Error writing (Not connected): %s",
                    strerror(errno));
            return SB_NOT_CONNECTED;
        }
        if (rc < 0) {          // got an error writing . Aborting
            log_err(sb, "recv(): Erro writing (Unknown): %s", strerror(errno));
            return SB_ERROR_UNKNOWN;
        }
        remain -= rc;
        buf += rc;
    } while (remain > 0);
    log_debug(sb, "recv() %d bytes", rc);
    return SB_OK;

}

int
net_send(sapo_broker_t *sb, _broker_server_t *srv, const char *bytes, size_t len)
{
    int rc = 0;
    uint16_t header[SB_NET_HEADER_SIZ];

    header[0] = htons( srv->srv.protocol );
    header[1] = htons( 0 );

    *(uint32_t *)(header+2) = htonl( len );

    log_info(sb, "");

    log_debug(sb, "net_send(): sending data (size: %d)", len);
    rc = _net_send(sb, srv->fd, (const char *) header, sizeof(header), SB_MSG_NOSIGPIPE);
    if( rc != SB_OK) {
        log_err(sb, "net_send(): failed to send header");
        return rc;
    }
    rc = _net_send(sb, srv->fd, bytes, len, SB_MSG_NOSIGPIPE);
    if( rc != SB_OK){
        log_err(sb, "net_send(): failed to send message");
        return rc;
    }
    return rc;
}

/* net_poll waits for packets from any connected server.
   returns the 1st server that writes to us.
FIXME: this is THREAD UNSAFE
    depends on servers' connect state stability during select
*/
_broker_server_t *
net_poll( sapo_broker_t *sb, struct timeval *tv)
{
    log_info(sb, "");
    /* try only once to connect to all srvs */
    int i = 0;
    int rc = 0;
    int num_srvs = 0;
    fd_set read_set;

    FD_ZERO(&read_set);
    /* listen to all connected servers */
    for(i = 0; i < sb->servers.server_count; i++ ) {
        if( sb->servers.server[i].connected ) {
            rc = sb->servers.server[i].fd;
            log_debug(sb, "srv[%d] ON fd:%d", i, rc);
            FD_SET( rc, &read_set);
            num_srvs = rc;
        }
    }


    /* wait on select for any packet */
    rc = select( num_srvs + 1, &read_set, NULL, NULL, tv);
    if( rc == 0) { // timed out
        log_debug(sb, "net_poll(): timed out");
    } else if( rc > 0) { // return server.
        for(i = 0; i < sb->servers.server_count; i++ ) {
            if( sb->servers.server[i].connected  &&
                 FD_ISSET( sb->servers.server[i].fd, &read_set) )
                return &(sb->servers.server[i]);
        }
    } else {
        /* error.. */
        log_err(sb, "net_poll: error on select(): %s", strerror(errno));
    }

    return NULL;
}

char *
net_recv( sapo_broker_t *sb, _broker_server_t *srv, int *buf_len)
{
    log_info(sb, "");
    uint16_t header[SB_NET_HEADER_SIZ];
    uint32_t len = 0;
    int rc = 0;
    char *buf;

    log_debug(sb, "net_recv(): waiting %d bytes", sizeof(header));
    rc = _net_recv(sb, srv->fd, (char *) header, sizeof(header), 0);

    if( srv->srv.protocol != ntohs( header[0] ) ) {
            log_err(sb, "net_recv: protocol in received header doesn't match connection setup.");
    }

    len = ntohl( *(uint32_t *)(header+2) );
    log_info(sb, "net_recv: incoming message of size: %d", len);
    buf = malloc( len );
    assert(buf);

    *buf_len = len;
    rc = _net_recv(sb, srv->fd, buf, *buf_len, 0);
    if( rc != SB_OK ) {
        free(buf);
        *buf_len = 0;
        return  NULL;
    }

    return buf;
}
