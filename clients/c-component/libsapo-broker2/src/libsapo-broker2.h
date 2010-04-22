#ifndef _SAPO_BROKER_H
#define _SAPO_BROKER_H

#include <stdint.h>
#include <stdlib.h>
#include <stdbool.h>
#include <sys/time.h>

// error codes
#define SB_OK                  0
#define SB_NO_MESSAGE       -100
#define SB_HAS_MESSAGE      -101
#define SB_ERROR            -102
#define SB_NO_ERROR         -103
#define SB_BAD_MESSAGE_TYPE -110
#define SB_NOT_CONNECTED    -120
#define SB_ERROR_UNKNOWN    -121
#define SB_NOT_INITIALIZED  -122

#define TRUE    1
#define FALSE   !TRUE

#define SB_PORT_OLD     3322
#define SB_PORT_UDP_OLD 3366
#define SB_PORT         3323

#define SB_BUFSIZ  512

#if !defined(uint_t)
 typedef unsigned int uint_t;
#endif

typedef enum { TCP = 0, UDP } transport_type_t;
typedef enum { SOAP = 0, PROTOBUF, THRIFT } protocol_type_t;
typedef enum { SB_QUEUE = 0, SB_TOPIC } destination_type_t;

typedef struct {
	char *hostname;
	uint16_t port;
	uint8_t transport;
	uint8_t protocol;
} broker_server_t;

typedef struct {
	char *name;
	uint8_t type;
	bool queue_autoack;
} broker_destination_t;


typedef struct {
	size_t 	payload_len;
	char * 	payload;
    char *  message_id;
    bool    acked;
	broker_destination_t origin;
	broker_destination_t destination;
	broker_server_t	     server;
} broker_msg_t;


typedef struct {
    size_t      payload_size;
    char *      payload;
    char *      message_id; /* optional; if NOT NULL: must be NULL terminated */
    uint64_t    expiration; /* optional */
    uint64_t    timestamp;  /* optional */
} broker_sendmsg_t;


typedef struct {
    broker_server_t srv;
    bool    connected;
    uint16_t  fail_count;
    int     socket_type;
    uint_t fd;
} _broker_server_t;


typedef struct {
    uint_t    server_count;   // nr of valid servers in array
    _broker_server_t *  server;  // servers array
    uint_t  array_count;      // nr/size of servers array.
} _broker_server_array_t;



typedef struct {
    uint_t dest_count;
    broker_destination_t *dest;
    uint_t array_count; // count of entries allocated.
} _destinations_array_t;


typedef struct {
    _broker_server_array_t servers;
    _destinations_array_t destinations;
    char last_error_msg[SB_BUFSIZ];
} sapo_broker_t;


sapo_broker_t *
broker_init( broker_server_t broker);

int
broker_destroy( sapo_broker_t *);

/* add more servers for failover */
int
broker_add_server( sapo_broker_t * sb, broker_server_t broker);

int
broker_subscribe( sapo_broker_t *sb, broker_destination_t destination);
int
broker_subscribe_topic( sapo_broker_t *sb, char *destination );
int
broker_subscribe_queue( sapo_broker_t *sb, char *destination, bool autoack);

/* TODO:
int
broker_unsubscribe(sapo_broker_t *sb, broker_destination_t destination);
int
broker_unsubscribe_topic(sapo_broker_t *sb, char *destination);
int
broker_unsubscribe_queue(sapo_broker_t *sb, char *destination);
*/

broker_msg_t *
broker_receive(sapo_broker_t *sb, struct timeval *timeout);

int
broker_msg_free( broker_msg_t * );

/* Acknowledge Message and free it */
int
broker_msg_ack( sapo_broker_t *sb, broker_msg_t *);

/* all the producer calls send only to the first active server */
int
broker_send( sapo_broker_t *sb, broker_destination_t destination, broker_sendmsg_t msg);

int
broker_publish(sapo_broker_t *sb, char *destination, char *msg, size_t size);
int
broker_enqueue(sapo_broker_t *sb, char *destination, char *msg, size_t size);

char *
broker_error( sapo_broker_t *sb );

#endif // _SAPO_BROKER_H
