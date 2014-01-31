#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include "sapo-broker2.h"
#include "broker_config.h"

#define QUEUE_TEMPLATE "/libsapo-broker2/%lld"
#define PAYLOAD_TEMPLATE "test %d"

void error(const char* str){
    fprintf(stderr, "ERROR: %s\n", str);
	exit(-1);
}

int main(int argc, char *argv[])
{
    sapo_broker_t *broker;
    broker_server_t server;
    broker_destination_t destination;

    server.hostname =   BROKER_SERVER;
    server.port =    BROKER_TCP_PORT;
    server.transport = SB_TCP;
    server.protocol = SB_PROTOBUF;

    destination.type = SB_QUEUE;
    destination.queue_autoack = TRUE;


    broker = broker_init( server );
    if (!broker) {
        error(broker_error(broker));
    }
    
	char* queue;
	asprintf(&queue, QUEUE_TEMPLATE, (long long int) time(NULL));


	//produce
    for(int count=0; count < QUEUE_ITERATIONS; count++) {
		char* payload;
		size_t payload_len = asprintf(&payload, PAYLOAD_TEMPLATE, count);
        if ( SB_OK != broker_enqueue( broker, queue, payload, payload_len ) ){
            error(broker_error(broker));
        }
		free(payload);
    }

	destination.name = queue;
	//consume
	if ( SB_OK != broker_subscribe(broker, destination) ) {
            error(broker_error(broker));
	}

    for(int count=0; count < QUEUE_ITERATIONS; count++) {
		char* payload;
		size_t payload_len = asprintf(&payload, PAYLOAD_TEMPLATE, count);

		broker_msg_t *message = broker_receive(broker, NULL);

		if( NULL == message ){
			error("NULL message");
		}else if( 0 != strncmp(message->payload, payload, payload_len) ){
			error("mismatch message");
		}
		
	}

    broker_destroy(broker);
	free(queue);

    exit(0);
}
