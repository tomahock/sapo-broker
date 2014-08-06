/* Generated by the protocol buffer compiler.  DO NOT EDIT! */

#ifndef PROTOBUF_C_broker_2eproto__INCLUDED
#define PROTOBUF_C_broker_2eproto__INCLUDED

#include "protobuf-c.h"

PROTOBUF_C_BEGIN_DECLS


typedef struct _SapoBroker__Atom SapoBroker__Atom;
typedef struct _SapoBroker__Atom__Parameter SapoBroker__Atom__Parameter;
typedef struct _SapoBroker__Atom__Header SapoBroker__Atom__Header;
typedef struct _SapoBroker__Atom__BrokerMessage SapoBroker__Atom__BrokerMessage;
typedef struct _SapoBroker__Atom__Publish SapoBroker__Atom__Publish;
typedef struct _SapoBroker__Atom__Poll SapoBroker__Atom__Poll;
typedef struct _SapoBroker__Atom__Accepted SapoBroker__Atom__Accepted;
typedef struct _SapoBroker__Atom__AcknowledgeMessage SapoBroker__Atom__AcknowledgeMessage;
typedef struct _SapoBroker__Atom__Subscribe SapoBroker__Atom__Subscribe;
typedef struct _SapoBroker__Atom__Unsubscribe SapoBroker__Atom__Unsubscribe;
typedef struct _SapoBroker__Atom__Notification SapoBroker__Atom__Notification;
typedef struct _SapoBroker__Atom__Fault SapoBroker__Atom__Fault;
typedef struct _SapoBroker__Atom__Ping SapoBroker__Atom__Ping;
typedef struct _SapoBroker__Atom__Pong SapoBroker__Atom__Pong;
typedef struct _SapoBroker__Atom__Authentication SapoBroker__Atom__Authentication;
typedef struct _SapoBroker__Atom__Action SapoBroker__Atom__Action;


/* --- enums --- */

typedef enum _SapoBroker__Atom__Action__ActionType {
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__PUBLISH = 0,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__POLL = 1,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__ACCEPTED = 2,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__ACKNOWLEDGE_MESSAGE = 3,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__SUBSCRIBE = 4,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__UNSUBSCRIBE = 5,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__NOTIFICATION = 6,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__FAULT = 7,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__PING = 8,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__PONG = 9,
  SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__AUTH = 10
} SapoBroker__Atom__Action__ActionType;
typedef enum _SapoBroker__Atom__DestinationType {
  SAPO_BROKER__ATOM__DESTINATION_TYPE__TOPIC = 0,
  SAPO_BROKER__ATOM__DESTINATION_TYPE__QUEUE = 1,
  SAPO_BROKER__ATOM__DESTINATION_TYPE__VIRTUAL_QUEUE = 2
} SapoBroker__Atom__DestinationType;

/* --- messages --- */

struct  _SapoBroker__Atom__Parameter
{
  ProtobufCMessage base;
  char *name;
  char *value;
};
#define SAPO_BROKER__ATOM__PARAMETER__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__parameter__descriptor) \
    , NULL, NULL }


struct  _SapoBroker__Atom__Header
{
  ProtobufCMessage base;
  size_t n_parameter;
  SapoBroker__Atom__Parameter **parameter;
};
#define SAPO_BROKER__ATOM__HEADER__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__header__descriptor) \
    , 0,NULL }


struct  _SapoBroker__Atom__BrokerMessage
{
  ProtobufCMessage base;
  char *message_id;
  ProtobufCBinaryData payload;
  protobuf_c_boolean has_expiration;
  int64_t expiration;
  protobuf_c_boolean has_timestamp;
  int64_t timestamp;
};
#define SAPO_BROKER__ATOM__BROKER_MESSAGE__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__broker_message__descriptor) \
    , NULL, {0,NULL}, 0,0, 0,0 }


struct  _SapoBroker__Atom__Publish
{
  ProtobufCMessage base;
  char *action_id;
  SapoBroker__Atom__DestinationType destination_type;
  char *destination;
  SapoBroker__Atom__BrokerMessage *message;
};
#define SAPO_BROKER__ATOM__PUBLISH__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__publish__descriptor) \
    , NULL, 0, NULL, NULL }


struct  _SapoBroker__Atom__Poll
{
  ProtobufCMessage base;
  char *action_id;
  char *destination;
  int64_t timeout;
};
#define SAPO_BROKER__ATOM__POLL__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__poll__descriptor) \
    , NULL, NULL, 0 }


struct  _SapoBroker__Atom__Accepted
{
  ProtobufCMessage base;
  char *action_id;
};
#define SAPO_BROKER__ATOM__ACCEPTED__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__accepted__descriptor) \
    , NULL }


struct  _SapoBroker__Atom__AcknowledgeMessage
{
  ProtobufCMessage base;
  char *action_id;
  char *message_id;
  char *destination;
};
#define SAPO_BROKER__ATOM__ACKNOWLEDGE_MESSAGE__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__acknowledge_message__descriptor) \
    , NULL, NULL, NULL }


struct  _SapoBroker__Atom__Subscribe
{
  ProtobufCMessage base;
  char *action_id;
  char *destination;
  SapoBroker__Atom__DestinationType destination_type;
};
#define SAPO_BROKER__ATOM__SUBSCRIBE__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__subscribe__descriptor) \
    , NULL, NULL, 0 }


struct  _SapoBroker__Atom__Unsubscribe
{
  ProtobufCMessage base;
  char *action_id;
  char *destination;
  SapoBroker__Atom__DestinationType destination_type;
};
#define SAPO_BROKER__ATOM__UNSUBSCRIBE__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__unsubscribe__descriptor) \
    , NULL, NULL, 0 }


struct  _SapoBroker__Atom__Notification
{
  ProtobufCMessage base;
  char *destination;
  char *subscription;
  SapoBroker__Atom__DestinationType destination_type;
  SapoBroker__Atom__BrokerMessage *message;
};
#define SAPO_BROKER__ATOM__NOTIFICATION__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__notification__descriptor) \
    , NULL, NULL, 0, NULL }


struct  _SapoBroker__Atom__Fault
{
  ProtobufCMessage base;
  char *action_id;
  char *fault_code;
  char *fault_message;
  char *fault_detail;
};
#define SAPO_BROKER__ATOM__FAULT__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__fault__descriptor) \
    , NULL, NULL, NULL, NULL }


struct  _SapoBroker__Atom__Ping
{
  ProtobufCMessage base;
  char *action_id;
};
#define SAPO_BROKER__ATOM__PING__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__ping__descriptor) \
    , NULL }


struct  _SapoBroker__Atom__Pong
{
  ProtobufCMessage base;
  char *action_id;
};
#define SAPO_BROKER__ATOM__PONG__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__pong__descriptor) \
    , NULL }


struct  _SapoBroker__Atom__Authentication
{
  ProtobufCMessage base;
  char *action_id;
  char *authentication_type;
  ProtobufCBinaryData token;
  char *user_id;
  size_t n_role;
  char **role;
};
#define SAPO_BROKER__ATOM__AUTHENTICATION__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__authentication__descriptor) \
    , NULL, NULL, {0,NULL}, NULL, 0,NULL }


struct  _SapoBroker__Atom__Action
{
  ProtobufCMessage base;
  SapoBroker__Atom__Publish *publish;
  SapoBroker__Atom__Poll *poll;
  SapoBroker__Atom__Accepted *accepted;
  SapoBroker__Atom__AcknowledgeMessage *ack_message;
  SapoBroker__Atom__Subscribe *subscribe;
  SapoBroker__Atom__Unsubscribe *unsubscribe;
  SapoBroker__Atom__Notification *notification;
  SapoBroker__Atom__Fault *fault;
  SapoBroker__Atom__Ping *ping;
  SapoBroker__Atom__Pong *pong;
  SapoBroker__Atom__Authentication *auth;
  SapoBroker__Atom__Action__ActionType action_type;
};
#define SAPO_BROKER__ATOM__ACTION__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__action__descriptor) \
    , NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0 }


struct  _SapoBroker__Atom
{
  ProtobufCMessage base;
  SapoBroker__Atom__Header *header;
  SapoBroker__Atom__Action *action;
};
#define SAPO_BROKER__ATOM__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&sapo_broker__atom__descriptor) \
    , NULL, NULL }


/* SapoBroker__Atom__Parameter methods */
void   sapo_broker__atom__parameter__init
                     (SapoBroker__Atom__Parameter         *message);
/* SapoBroker__Atom__Header methods */
void   sapo_broker__atom__header__init
                     (SapoBroker__Atom__Header         *message);
/* SapoBroker__Atom__BrokerMessage methods */
void   sapo_broker__atom__broker_message__init
                     (SapoBroker__Atom__BrokerMessage         *message);
/* SapoBroker__Atom__Publish methods */
void   sapo_broker__atom__publish__init
                     (SapoBroker__Atom__Publish         *message);
/* SapoBroker__Atom__Poll methods */
void   sapo_broker__atom__poll__init
                     (SapoBroker__Atom__Poll         *message);
/* SapoBroker__Atom__Accepted methods */
void   sapo_broker__atom__accepted__init
                     (SapoBroker__Atom__Accepted         *message);
/* SapoBroker__Atom__AcknowledgeMessage methods */
void   sapo_broker__atom__acknowledge_message__init
                     (SapoBroker__Atom__AcknowledgeMessage         *message);
/* SapoBroker__Atom__Subscribe methods */
void   sapo_broker__atom__subscribe__init
                     (SapoBroker__Atom__Subscribe         *message);
/* SapoBroker__Atom__Unsubscribe methods */
void   sapo_broker__atom__unsubscribe__init
                     (SapoBroker__Atom__Unsubscribe         *message);
/* SapoBroker__Atom__Notification methods */
void   sapo_broker__atom__notification__init
                     (SapoBroker__Atom__Notification         *message);
/* SapoBroker__Atom__Fault methods */
void   sapo_broker__atom__fault__init
                     (SapoBroker__Atom__Fault         *message);
/* SapoBroker__Atom__Ping methods */
void   sapo_broker__atom__ping__init
                     (SapoBroker__Atom__Ping         *message);
/* SapoBroker__Atom__Pong methods */
void   sapo_broker__atom__pong__init
                     (SapoBroker__Atom__Pong         *message);
/* SapoBroker__Atom__Authentication methods */
void   sapo_broker__atom__authentication__init
                     (SapoBroker__Atom__Authentication         *message);
/* SapoBroker__Atom__Action methods */
void   sapo_broker__atom__action__init
                     (SapoBroker__Atom__Action         *message);
/* SapoBroker__Atom methods */
void   sapo_broker__atom__init
                     (SapoBroker__Atom         *message);
size_t sapo_broker__atom__get_packed_size
                     (const SapoBroker__Atom   *message);
size_t sapo_broker__atom__pack
                     (const SapoBroker__Atom   *message,
                      uint8_t             *out);
size_t sapo_broker__atom__pack_to_buffer
                     (const SapoBroker__Atom   *message,
                      ProtobufCBuffer     *buffer);
SapoBroker__Atom *
       sapo_broker__atom__unpack
                     (ProtobufCAllocator  *allocator,
                      size_t               len,
                      const uint8_t       *data);
void   sapo_broker__atom__free_unpacked
                     (SapoBroker__Atom *message,
                      ProtobufCAllocator *allocator);
/* --- per-message closures --- */

typedef void (*SapoBroker__Atom__Parameter_Closure)
                 (const SapoBroker__Atom__Parameter *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Header_Closure)
                 (const SapoBroker__Atom__Header *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__BrokerMessage_Closure)
                 (const SapoBroker__Atom__BrokerMessage *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Publish_Closure)
                 (const SapoBroker__Atom__Publish *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Poll_Closure)
                 (const SapoBroker__Atom__Poll *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Accepted_Closure)
                 (const SapoBroker__Atom__Accepted *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__AcknowledgeMessage_Closure)
                 (const SapoBroker__Atom__AcknowledgeMessage *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Subscribe_Closure)
                 (const SapoBroker__Atom__Subscribe *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Unsubscribe_Closure)
                 (const SapoBroker__Atom__Unsubscribe *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Notification_Closure)
                 (const SapoBroker__Atom__Notification *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Fault_Closure)
                 (const SapoBroker__Atom__Fault *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Ping_Closure)
                 (const SapoBroker__Atom__Ping *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Pong_Closure)
                 (const SapoBroker__Atom__Pong *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Authentication_Closure)
                 (const SapoBroker__Atom__Authentication *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom__Action_Closure)
                 (const SapoBroker__Atom__Action *message,
                  void *closure_data);
typedef void (*SapoBroker__Atom_Closure)
                 (const SapoBroker__Atom *message,
                  void *closure_data);

/* --- services --- */


/* --- descriptors --- */

extern const ProtobufCMessageDescriptor sapo_broker__atom__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__parameter__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__header__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__broker_message__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__publish__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__poll__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__accepted__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__acknowledge_message__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__subscribe__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__unsubscribe__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__notification__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__fault__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__ping__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__pong__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__authentication__descriptor;
extern const ProtobufCMessageDescriptor sapo_broker__atom__action__descriptor;
extern const ProtobufCEnumDescriptor    sapo_broker__atom__action__action_type__descriptor;
extern const ProtobufCEnumDescriptor    sapo_broker__atom__destination_type__descriptor;

PROTOBUF_C_END_DECLS


#endif  /* PROTOBUF_broker_2eproto__INCLUDED */