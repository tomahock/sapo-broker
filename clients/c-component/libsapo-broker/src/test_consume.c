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

#define QUEUE_NAME  "/my/test/queue"
#define PAYLOAD     "ping"

int main(int argc, char *argv[])
{
    int i = 0;
    int count = 0;
    SAPO_BROKER_T *sb;
    BrokerMessage *m;
    int dest_type = EQUEUE_QUEUE;
    char *dest_name = QUEUE_NAME;

    sb = sb_new( HOST, TCP_PORT, SB_TYPE_TCP);
    if (!sb) {
        printf("%s", sb_error(sb));
        exit(-1);
    }

    if( argc >= 2 ) {
        if( !strncmp("-topic", argv[1], 4 ) ) {
            dest_type = EQUEUE_TOPIC;
        } else {
            dest_type = EQUEUE_QUEUE;
        }

        if( argc >= 3) {
            dest_name = argv[2];
        } else {
            dest_name = QUEUE_NAME;
        }
        if( argc >= 4) {
            count = atoi(argv[3]);
        }

    } else {
        dest_type = EQUEUE_QUEUE;
        dest_name = QUEUE_NAME;
    }

    if( dest_type == EQUEUE_QUEUE)
        printf("QUEUE ");
    else
        printf("TOPIC ");
    printf("%s\n", dest_name);

    if (sb_connect(sb) != SB_OK) {
        printf("%s", sb_error());
        exit(-1);
    }

    if (sb_subscribe(sb, dest_type, dest_name) != SB_OK) {
        printf("%s", sb_error(sb));
        exit(-1);
    }

    printf("consuming %d msgs\n", count);
    /* by default: process 2^31-1 msgs, depends on int overflow */
    for(; i <= count; i++) {
        m = sb_receive(sb);
        printf("%s\n", m->payload);

        if( dest_type == EQUEUE_QUEUE ) {
            sb_send_ack( sb, m);
        }

        sb_free_message(m);
	}

    printf("Exiting: %s\n", sb_error(sb));
    /* pedantic mode, good for the server */
    sb_destroy( sb );
    return 0;
}
