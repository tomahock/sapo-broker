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

#define TOPIC       "/my/test/topic"
#define PAYLOAD     "ping0000000000000000000000000000000000000000111111111111111"

int main(int argc, char *argv[])
{
    int rc = 0;
    char *payload = PAYLOAD;
    sapo_broker_t *sb;
    broker_server_t server;

    server.hostname =   HOST;
    server.port =    SB_PORT;
    server.transport =   SB_TCP;
    server.protocol = SB_PROTOBUF;

    if( argc >= 2 ) {
        if( !strncmp("-udp", argv[1], 4 ) ) {
            printf("UDP ");
            server.transport = SB_UDP;
        } else {
            printf("TCP ");
            server.transport = SB_TCP;
        }

        if( argc >= 3)
            payload = argv[2];
    }

    sb = broker_init( server );
    if (!sb) {
        printf("%s", broker_error(sb));
        exit(-1);
    }

    /* or:
       broker_destination_t dest;
       dest.name = TOPIC_NAME;
       dest.type = TOPIC;
       broker_send ( sb, destination, buf, len )
       */
    printf("publish to topic: %s, payload: %s\n", TOPIC, payload);
    rc = broker_publish( sb, TOPIC, payload, strlen(payload) );

    if ( rc != SB_OK) {
        printf("%s", broker_error(sb));
        exit(-1);
    }

    exit(0);
}
