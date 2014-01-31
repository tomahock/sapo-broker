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
#define PORT        TCP_PORT
#define TYPE        SB_TYPE_TCP // or SB_TYPE_UDP

#define TOPIC       "cbroker@/my/test/topic"

int main(int argc, char *argv[])
{
    SAPO_BROKER_T *sb;
    BrokerMessage *m;

    sb = sb_new(HOST, PORT, TYPE);

    if (!sb) {
        printf("%s", sb_error());
        exit(-1);
    }

    if (sb_connect(sb) != SB_OK) {
        printf("%s", sb_error());
        exit(-1);
    }

    if (sb_subscribe(sb, EQUEUE_VIRTUAL_QUEUE, TOPIC) != SB_OK) {
        printf("%s", sb_error());
        exit(-1);
    }

    while ((m = sb_receive(sb)) != NULL) {
        printf("%s\n", m->payload);
		sb_send_ack( sb, m);
		sb_free_message(m);
	}

    printf("Exiting: %s\n", sb_error());
}
