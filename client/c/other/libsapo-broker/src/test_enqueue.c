#define _XOPEN_SOURCE 600
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <stdint.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <errno.h>

#include "sapo_broker.h"

#define TCP_PORT    3322
#define UDP_PORT    3366
#define HOST        "127.0.0.1"

#define QUEUE       "/my/test/queue"
#define PAYLOAD     "ping"

int main(int argc, char *argv[])
{
    int rc = 0;
    int count = 1;
    char *payload = PAYLOAD;
    SAPO_BROKER_T *sb;

    int transport_type = SB_TYPE_TCP;;
    int port = TCP_PORT;

    if( argc >= 2 ) {
        if( !strncmp("-udp", argv[1], 4 ) ) {
            printf("UDP ");
            transport_type = SB_TYPE_UDP;
            port = UDP_PORT;
        } else {
            printf("TCP ");
            transport_type = SB_TYPE_TCP;
            port = TCP_PORT;
        }
        if( argc >= 3 ) {
            payload = argv[2];
        }
        if( argc >= 4 ) {
            count = atoi(argv[3]);
        }
    }

    sb = sb_new( HOST, port, transport_type );
    if (!sb) {
        printf("1%s", sb_error(sb));
        exit(-1);
    }

    if (sb_connect(sb) != SB_OK) {
        printf("2%s", sb_error());
        exit(-1);
    }

    printf("enqueue %d times to queue: %s, payload: %s\n", count, QUEUE, payload);
    for(; count > 0; count--) {
        rc = sb_publish( sb, EQUEUE_QUEUE, QUEUE, payload );
        if ( rc != SB_OK) {
            printf("3%s", sb_error(sb));
            exit(-1);
        }
    }

    sb_destroy(sb);
    exit(0);
}
