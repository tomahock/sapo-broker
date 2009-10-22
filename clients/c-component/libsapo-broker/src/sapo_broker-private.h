#ifndef _SAPO_BROKER_PRIVATE_H_
#define _SAPO_BROKER_PRIVATE_H_

/* broker message header fields */
typedef struct {
    uint16_t enc_type;
    uint16_t enc_version;
    uint32_t length;
} broker_msg_header;

/* broker message fields */
typedef struct {
    broker_msg_header header;
    void *payload;
} broker_msg;

/* broker message payload */
typedef struct {
    size_t size;
    void *data;
} broker_payload;

#endif /* _SAPO_BROKER_PRIVATE_H_ */

