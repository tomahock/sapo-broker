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

#define HOST        "127.0.0.1"

#define QUEUE_NAME  "/my/test/queue"
#define PAYLOAD     "ping"

int main(int argc, char *argv[])
{
    sapo_broker_t *sb;
    broker_msg_t *m;
    broker_server_t server;
    broker_destination_t dest;

    server.hostname =   HOST;
    server.port =    SB_PORT;
    server.transport = TCP;
    server.protocol = PROTOBUF;

    sb = broker_init( server );
    if (!sb) {
        printf("%s", broker_error(sb));
        exit(-1);
    }

    if( argc >= 2 ) {
        if( !strncmp("-topic", argv[1], 4 ) ) {
            dest.type = SB_TOPIC;
        } else {
            dest.type = SB_QUEUE;
        }

        if( argc == 3) {
            dest.name = argv[2];
        } else {
            dest.name = QUEUE_NAME;
        }

    } else {
        dest.type = SB_QUEUE;
        dest.name = QUEUE_NAME;
    }
    dest.queue_autoack = FALSE;

    if( dest.type == SB_QUEUE)
        printf("QUEUE ");
    else
        printf("TOPIC ");
    printf("%s\n", dest.name);
    /* broker_destination_t dest;
       dest.name = QUEUE;
       dest.type = SB_QUEUE;
       dest.queue_autoack = true;
       broker_subscribe ( sb, destination)
       or
       bool auto_ack = TRUE; // or FALSE
       broker_subscribe_queue( sb, "destination", auto_ack);
       or
       broker_subscribe_topic( sb, "destination" );

       */
    if (broker_subscribe(sb, dest) != SB_OK) {
        printf("%s", broker_error(sb));
        exit(-1);
    }

    while ((m = broker_receive(sb, NULL)) != NULL) {
        printf("%s\n", m->payload);

        if( !strncmp(m->payload, "EXIT", 4 ) )
            break;

        if( dest.type == SB_QUEUE && dest.queue_autoack == FALSE ) {
            broker_msg_ack( sb, m);
        } else {
            broker_msg_free(m);
        }
	}

    printf("Exiting: %s\n", broker_error(sb));
    /* pedantic mode, good for the server */
    broker_destroy( sb );
    return 0;
}
