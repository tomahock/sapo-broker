#define _SVID_SOURCE
#include <assert.h>
#include <errno.h>
#include <netdb.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>


#include "sapo-broker2.h"
#include "broker_internals.h"
#include "net.h"

static int
_server_connect( sapo_broker_t *sb, _broker_server_t *server )
{
    struct hostent *he = NULL;
    struct sockaddr_in server_addr;
    int rc = 0;

    if(!sb)
        return SB_NOT_INITIALIZED;

    /* don't lock because parent functions of this one allready handle locking */

    if (server && server->connected) {
        return SB_OK;
    }

    log_debug(sb, "connect(): trying %s:%d", server->srv.hostname , server->srv.port);

    /* FIXME: do this sooner and only once on server_add */
    if( server->srv.transport == SB_TCP )
        server->socket_type = SOCK_STREAM;
    else
        server->socket_type = SOCK_DGRAM;

    rc = socket(AF_INET, server->socket_type, 0);
    if ( rc < 0) {
        log_err(sb, "connect(): (error creqting socket) %s", strerror(errno));
        pthread_mutex_unlock(sb->lock);
        return SB_ERROR;
    }
    server->fd = rc;

    he = gethostbyname( server->srv.hostname );
    if ( he == NULL) {
        server->connected = FALSE;
        server->fail_count++;
        log_err(sb, "gethostbyhostname: %s", hstrerror(h_errno));
        pthread_mutex_unlock(sb->lock);
        return SB_ERROR;
    }

    bzero(&server_addr, sizeof(server_addr));
    memcpy(&server_addr.sin_addr, he->h_addr_list[0], he->h_length);
    server_addr.sin_family = AF_INET;   // Internet address family
    server_addr.sin_port = htons(server->srv.port);   // Server port

    rc = connect(server->fd, (struct sockaddr *) &server_addr,
          sizeof(server_addr));
    if ( rc < 0) {
        server->fail_count++;
        log_err(sb, "connect(): (can't conntect) %s", strerror(errno));
        close(server->fd);
        server->connected = FALSE;
        return SB_ERROR;
    }
    server->fail_count = 0;
    server->connected = 1;

    return SB_OK;
}


_broker_server_t *
_broker_server_get_active( sapo_broker_t *sb)
{
    uint_t i;
    _broker_server_t *srv = NULL;

    if( NULL == sb ||
        NULL == sb->servers.server )
        return NULL;

    pthread_mutex_lock(sb->lock);

    srv = sb->servers.server;
    for(i= 0; i < sb->servers.server_count; i++) {
        if ( srv[i].connected ) {
            pthread_mutex_unlock(sb->lock);
            return srv;
        }
    }

    log_info(sb, "broker_get_active(): not connected, trying servers in order.");
    for(i = 0; i < sb->servers.server_count; i++) {
        if ( ! _server_connect( sb, &srv[i] ) ) {
            pthread_mutex_unlock(sb->lock);
            log_info(sb, "connected to: %s:%d", srv[i].srv.hostname, srv[i].srv.port);
            broker_resubscribe_destinations(sb, srv);
            return srv;
        }
    }

    log_err(sb, "server_get_active(): couldn't connect to any of the servers.");

    pthread_mutex_unlock(sb->lock);
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

    pthread_mutex_lock(sb->lock);

    srv = sb->servers.server;
    for(i = 0; i < sb->servers.server_count; i++) {
        if( ! srv[i].connected ) {
            _server_connect(sb, &srv[i]);
        }
    }

    pthread_mutex_unlock(sb->lock);
    return SB_OK;
}

static int
_broker_server_disconnect(sapo_broker_t *sb, _broker_server_t *srv)
{
    if( srv->connected ) {
        /* best effort */
        shutdown(srv->fd, 2); /* bidirectional shutdown */
        close(srv->fd);
        srv->fd = -1; /* so that if we use it something definitely fails */
        srv->fail_count = 0;
        srv->connected = 0;
    }
    return 0;
}


int
_broker_server_disconnect_all(sapo_broker_t *sb)
{
    uint_t i;
    _broker_server_t *srv = NULL;

    if( NULL == sb ||
        NULL == sb->servers.server )
        return SB_ERROR;

    pthread_mutex_lock(sb->lock);

    srv = sb->servers.server;
    for(i = 0; i < sb->servers.server_count; i++) {
        _broker_server_disconnect(sb, &srv[i]);
    }

    pthread_mutex_unlock(sb->lock);
    return SB_OK;
}

static int
_net_send(sapo_broker_t *sb, int socket, const char *buf, size_t len, int flags)
{
    size_t remain = len;
    ssize_t rc = 0;
    do {
        rc = send(socket, buf, remain, flags);

        if(rc < 0){
            /* error writing */
            if (errno == EINTR) {
                continue;
            }else if (errno == ENOTCONN || errno == EPIPE || errno == ECONNRESET) {
                log_err(sb, "send(): Error writing (Not connected [%d]): %s",
                        errno, strerror(errno));
                return SB_NOT_CONNECTED;
            }else {
                log_err(sb, "send(): Erro writing (Unknown [%d]): %s",
                        errno, strerror(errno));
                return SB_ERROR_UNKNOWN;
            }
        }else{
            remain -= rc;
            buf += rc;
        }
    } while (remain > 0);
    log_debug(sb, "send() %zd bytes", rc);
    return SB_OK;
}

static int
_net_recv(sapo_broker_t *sb, int socket, char *buf, size_t len, int flags)
{
    size_t remain = len;
    ssize_t rc = 0;
    do {
        rc = recv(socket, buf, remain, flags);

        if (rc < 0){
            /* error reading */
            if (errno == EINTR) {
                continue;
            } else if(errno == ENOTCONN || errno == ECONNREFUSED || errno == EPIPE){
                log_err(sb, "recv(): Error reading (Not connected [%d]): %s",
                        errno, strerror(errno));
                return SB_NOT_CONNECTED;
            }else{
                log_err(sb, "recv(): Error reading (Unknown [%d]): %s",
                        errno, strerror(errno));
                return SB_ERROR_UNKNOWN;
            }
        }else if( 0 == rc ){ /* clean shutdown */
            log_err(sb, "recv(): Error writing (Not connected): clean shutdown from server");
            return SB_NOT_CONNECTED;
        }else{
            remain -= rc;
            buf += rc;
        }
    } while (remain > 0);
    log_debug(sb, "recv() %zd bytes", rc);
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

    log_debug(sb, "net_send(): sending data (size: %zd)", len);


    if(srv->socket_type == SB_UDP)
    {
    // UDP
     
    char*  buffer = malloc( sizeof(header) + len);

    memcpy(buffer, header, sizeof(header));

    memcpy(buffer + sizeof(header), bytes, len);

    pthread_mutex_lock(srv->lock_w);
    rc = _net_send(sb, srv->fd, (const char *) buffer,  sizeof(header) + len, SB_MSG_NOSIGPIPE);
    free(buffer);
    if( rc != SB_OK) {
        log_err(sb, "net_send(): failed to send header");
        goto err;
    }
    }
    else
    {
    // TCP
        pthread_mutex_lock(srv->lock_w);
        rc = _net_send(sb, srv->fd, (const char *) header, sizeof(header), SB_MSG_NOSIGPIPE);
        if( rc != SB_OK) {
        log_err(sb, "net_send(): failed to send header");
        goto err;
        }
        rc = _net_send(sb, srv->fd, bytes, len, SB_MSG_NOSIGPIPE);
        if( rc != SB_OK){
        log_err(sb, "net_send(): failed to send message");
        goto err;
        }
        
    }
    pthread_mutex_unlock(srv->lock_w);
    return rc;

err:
    pthread_mutex_unlock(srv->lock_w);

    if( SB_NOT_CONNECTED == rc) {
        pthread_mutex_lock(sb->lock);
        _broker_server_disconnect(sb, srv);
        pthread_mutex_unlock(sb->lock);
    }

    return  rc;
}

/* net_poll waits for packets from any connected server.
   returns the 1st server that writes to us.
*/
_broker_server_t *
net_poll( sapo_broker_t *sb, struct timeval *tv)
{
    if(!sb)
        return NULL;

    pthread_mutex_lock(sb->lock);

    /* try only once to connect to all srvs */
    uint_t i = 0;
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
            if( sb->servers.server[i].connected  && FD_ISSET( sb->servers.server[i].fd, &read_set) ) {
                pthread_mutex_unlock(sb->lock);
                return &(sb->servers.server[i]);
            }
        }
    } else {
        /* error.. */
        log_err(sb, "net_poll: error on select(): %s", strerror(errno));
    }

    pthread_mutex_unlock(sb->lock);

    return NULL;
}

char *
net_recv( sapo_broker_t *sb, _broker_server_t *srv, int *buf_len)
{
    uint16_t header[SB_NET_HEADER_SIZ];
    uint32_t len = 0;
    int rc = 0;
    char *buf;

    /* clean header */
    bzero( header, sizeof(header) );

    pthread_mutex_lock(srv->lock_r);

    log_debug(sb, "net_recv(): waiting %zd bytes", sizeof(header));
    rc = _net_recv(sb, srv->fd, (char *) header, sizeof(header), 0);
    if( rc != SB_OK) {
        goto err;
    }

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
        goto err;
    }
    pthread_mutex_unlock(srv->lock_r);
    return buf;

err:
    pthread_mutex_unlock(srv->lock_r);
    if( rc == SB_NOT_CONNECTED ) {
        pthread_mutex_lock(sb->lock);
        _broker_server_disconnect(sb, srv);
        pthread_mutex_unlock(sb->lock);
    }

    *buf_len = rc;
    return  NULL;

}
