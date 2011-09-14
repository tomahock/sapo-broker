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

#include "sapo-broker2.h"

#define HOST        "127.0.0.1"

#define QUEUE       "/my/test/queue"
#define PAYLOAD     "ping"

int main(int argc, char *argv[])
{
    int rc = 0;
    int count = 1;
    char *payload = PAYLOAD;
    sapo_broker_t *sb;
    broker_server_t server;

    server.hostname =   HOST;
    server.port =    SB_PORT;
    server.transport = SB_TCP;
    server.protocol = SB_PROTOBUF;

    if( argc >= 2 ) {
        if( !strncmp("-udp", argv[1], 4 ) ) {
            printf("UDP ");
            server.transport = SB_UDP;
        } else {
            printf("TCP ");
            server.transport = SB_TCP;
        }
        if( argc >= 3 ) {
            payload = argv[2];
        }
        if( argc >= 4 ) {
            count = atoi(argv[3]);
        }
    }


    sb = broker_init( server );
    if (!sb) {
        printf("%s", broker_error(sb));
        exit(-1);
    }

    printf("enqueue %d times to queue: %s, payload: %s\n", count, QUEUE, payload);
    for(; count > 0; count--) {
        rc = broker_enqueue( sb, QUEUE, payload, strlen(payload) );
        if ( rc != SB_OK) {
            printf("%s", broker_error(sb));
            exit(-1);
        }
    }

    broker_destroy(sb);

    exit(0);
}
