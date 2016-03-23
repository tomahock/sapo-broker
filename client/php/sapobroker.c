#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "php.h"
#include "php_ini.h"
#include "ext/standard/info.h"
#include "php_sapobroker.h"
#include <sapo-broker2.h>

#define PHP_BROKER_SERVER_T_RES_NAME "Broker Server"
#define PHP_SAPO_BROKER_T_RES_NAME "Sapo Broker"
#define PHP_BROKER_MSG_T_RES_NAME "Sapo Broker Message"
#define PHP_SAPO_BROKER_EXTENSION_VERSION "0.4"

static int le_sapobroker;
int le_broker_server_t;
int le_sapo_broker_t;
int le_broker_msg_t;

const zend_function_entry sapobroker_functions[] = {
    PHP_FE(broker_init, NULL)
    PHP_FE(broker_destroy, NULL)
    PHP_FE(broker_add_server, NULL)
    PHP_FE(broker_subscribe, NULL)
    PHP_FE(broker_subscribe_topic, NULL)
    PHP_FE(broker_subscribe_queue, NULL)
    PHP_FE(broker_subscribe_virtual_queue, NULL)
    PHP_FE(broker_receive, NULL)
    PHP_FE(broker_msg_free, NULL)
    PHP_FE(broker_msg_ack, NULL)
    PHP_FE(broker_send, NULL)
    PHP_FE(broker_publish, NULL)
    PHP_FE(broker_enqueue, NULL)
    PHP_FE(broker_error, NULL)
    PHP_FE(broker_msg_decode, NULL)
    {NULL, NULL, NULL} /* Must be the last line in sapobroker_functions[] */
};

zend_module_entry sapobroker_module_entry = {
#if ZEND_MODULE_API_NO >= 20010901
    STANDARD_MODULE_HEADER,
#endif
    "sapobroker",
    sapobroker_functions,
    PHP_MINIT(sapobroker),
    PHP_MSHUTDOWN(sapobroker),
    NULL, //PHP_RINIT(sapobroker), * Replace with NULL if there's nothing to do at request start */
    NULL, //PHP_RSHUTDOWN(sapobroker), /* Replace with NULL if there's nothing to do at request end */
    PHP_MINFO(sapobroker),
#if ZEND_MODULE_API_NO >= 20010901
    PHP_SAPO_BROKER_EXTENSION_VERSION, /* Replace with version number for your extension */
#endif
    STANDARD_MODULE_PROPERTIES
};

#ifdef COMPILE_DL_SAPOBROKER
    ZEND_GET_MODULE(sapobroker)
#endif

PHP_MINIT_FUNCTION(sapobroker) {
	// defined in sapobroker2.h
	REGISTER_LONG_CONSTANT("SB_PORT", SB_PORT, CONST_CS | CONST_PERSISTENT);
    REGISTER_LONG_CONSTANT("SB_TCP", SB_TCP, CONST_CS | CONST_PERSISTENT);
	REGISTER_LONG_CONSTANT("SB_UDP", SB_UDP, CONST_CS | CONST_PERSISTENT);
	REGISTER_LONG_CONSTANT("SB_QUEUE", SB_QUEUE, CONST_CS | CONST_PERSISTENT);
	REGISTER_LONG_CONSTANT("SB_TOPIC", SB_TOPIC, CONST_CS | CONST_PERSISTENT);
	REGISTER_LONG_CONSTANT("SB_SOAP", SB_SOAP, CONST_CS | CONST_PERSISTENT);
	REGISTER_LONG_CONSTANT("SB_PROTOBUF", SB_PROTOBUF, CONST_CS | CONST_PERSISTENT);
	REGISTER_LONG_CONSTANT("SB_THRIFT", SB_THRIFT, CONST_CS | CONST_PERSISTENT);

	le_broker_server_t = zend_register_list_destructors_ex(NULL, NULL, PHP_BROKER_SERVER_T_RES_NAME, module_number);
    le_sapo_broker_t = zend_register_list_destructors_ex(NULL, NULL, PHP_SAPO_BROKER_T_RES_NAME, module_number);
    le_broker_msg_t = zend_register_list_destructors_ex(NULL, NULL, PHP_BROKER_MSG_T_RES_NAME, module_number);
    return SUCCESS;
}

PHP_MSHUTDOWN_FUNCTION(sapobroker) {return SUCCESS;}
PHP_RINIT_FUNCTION(sapobroker) {return SUCCESS;}
PHP_RSHUTDOWN_FUNCTION(sapobroker) {return SUCCESS;}
PHP_MINFO_FUNCTION(sapobroker) {
    php_info_print_table_start();
    php_info_print_table_row(2, "Sapo Broker support", "enabled");
    php_info_print_table_row(2, "Version", PHP_SAPO_BROKER_EXTENSION_VERSION);
    php_info_print_table_end();
}

/* ADD BROKER SERVER_T TO BROKER HANDLE_T */
PHP_FUNCTION(broker_add_server) {
    int argc = ZEND_NUM_ARGS();
    zend_string *hostname;
    long port;
    long transport, protocol;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rSlll", &zsapo_broker, &hostname, &port, &transport, &protocol) == FAILURE)
        RETURN_FALSE;

    if (sapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);

    // alloc new server_t, copy and pass to libsapobroker
    broker_server_t *server = emalloc(sizeof(broker_server_t));
    server->hostname = estrndup(hostname->val, hostname->len);
    server->port = port;
    server->transport = transport;
    server->protocol = protocol;

    int result = broker_add_server(sapo_broker, *server);

    RETURN_LONG(result);
}

/* DEALLOC A BROKER HANDLE */
PHP_FUNCTION(broker_destroy) {
    int argc = ZEND_NUM_ARGS();
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "r", &zsapo_broker) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    int retval = broker_destroy(sapo_broker);
    zend_list_delete(Z_RES_P(zsapo_broker));

    RETURN_TRUE;
}

/* ENQUEUE A MESSAGE */
PHP_FUNCTION(broker_enqueue) {
    zend_string *queue = NULL;
    zend_string *message = NULL;
    int argc = ZEND_NUM_ARGS();
    zval *zsapo_broker = NULL;
    int retval;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rSS", &zsapo_broker, &queue, &message) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    retval = broker_enqueue(sapo_broker, queue->val, message->val, message->len);
    RETURN_LONG(retval);
}

/* GET LAST ERROR */
PHP_FUNCTION(broker_error) {
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "r", &zsapo_broker) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    char *msg = broker_error(sapo_broker);

    RETURN_STRING(msg);
}

/* ADDITIONAL FUNCTION TO INITIALIZE A BROKER_SERVER_T */
PHP_FUNCTION(broker_init) {
    int argc = ZEND_NUM_ARGS();
    zend_string *hostname;
    long port;
    long transport, protocol;

    if (zend_parse_parameters(argc TSRMLS_CC, "Slll", &hostname, &port, &transport, &protocol) == FAILURE)
        RETURN_FALSE;

    // alloc this or die
    broker_server_t *server = emalloc(sizeof(broker_server_t));
    server->hostname = estrndup(hostname->val, hostname->len);
    server->port = port;
    server->transport = transport;
    server->protocol = protocol;
    sapo_broker_t *abroker = broker_init(*server);

    ZVAL_RES(return_value, zend_register_resource(abroker, le_sapo_broker_t));
//    ZEND_REGISTER_RESOURCE(return_value, abroker, le_sapo_broker_t);
}

/* ACK A MESSAGE */
PHP_FUNCTION(broker_msg_ack) {
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    zval *zsapo_broker = NULL;
    zval *zbroker_msg = NULL;
    sapo_broker_t *sapo_broker;
    broker_msg_t *broker_msg;

    if (zend_parse_parameters(argc TSRMLS_CC, "rr", &zsapo_broker, &zbroker_msg) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL || zbroker_msg == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    broker_msg = (broker_msg_t*)zend_fetch_resource_ex(zbroker_msg, PHP_BROKER_MSG_T_RES_NAME, le_broker_msg_t);

    // call msg ack (libsapobroker frees the message after sending the ACK)
    int result = broker_msg_ack(sapo_broker, broker_msg);
    RETURN_LONG(result);
}

/* FREE A MESSAGE_T */
PHP_FUNCTION(broker_msg_free) {
    int argc = ZEND_NUM_ARGS();
    int message_id = -1;
    zval *message = NULL;

    if (zend_parse_parameters(argc TSRMLS_CC, "r", &message) == FAILURE)
        RETURN_FALSE;

    if (message == NULL)
        RETURN_FALSE;

    //zend_fetch_resource(???, ???, message, message_id, "???", ???_rsrc_id);
}

/* PUBLISH TO TOPIC */
PHP_FUNCTION(broker_publish) {
    zend_string *topic = NULL;
    zend_string *message = NULL;
    int argc = ZEND_NUM_ARGS();
    zval *zsapo_broker = NULL;
    int retval;
    int topic_len;
    int message_len;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rSS", &zsapo_broker, &topic, &message ) == FAILURE) {
        RETURN_FALSE;
    }

    if (zsapo_broker == NULL){
        RETURN_FALSE;
    }

    if((sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t)) == NULL) {
        RETURN_FALSE;
    }

    retval = broker_publish(sapo_broker, topic->val, message->val, message->len);

    RETURN_LONG(retval);
}

/* Decode a message */
PHP_FUNCTION(broker_msg_decode) {
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;

    zval *zbroker_msg = NULL;

    broker_msg_t *message;

    if (zend_parse_parameters(argc TSRMLS_CC, "r", &zbroker_msg) == FAILURE)
        RETURN_FALSE;

    if (zbroker_msg == NULL)
        RETURN_FALSE;

    message = (broker_msg_t*)zend_fetch_resource_ex(zbroker_msg, PHP_BROKER_MSG_T_RES_NAME, le_broker_msg_t);

    char *payload = estrndup(message->payload, message->payload_len);
    char *message_id = estrndup(message->message_id, strlen(message->message_id));

    // alloc return array
    array_init(return_value);
    add_assoc_long(return_value, "payload_len", message->payload_len);
    add_assoc_stringl(return_value, "payload", payload, message->payload_len);
    add_assoc_stringl(return_value, "message_id", message_id, strlen(message->message_id));
    add_assoc_bool(return_value, "acked", message->acked);

    zval *destination_arr;
    zval *origin_arr;

    // again, thank you Zend for such a lame naming scheme. not mentioning CAPS, of course :)
    ALLOC_INIT_ZVAL(destination_arr);
    ALLOC_INIT_ZVAL(origin_arr);

    // fill destination array
    //char *dest_name = estrndup(message->destination.name, strlen(message->destination.name));
    array_init(destination_arr);
    //add_assoc_stringl(destination_arr, "name", dest_name, strlen(message->destination.name), 1);
    add_assoc_long(destination_arr, "type", message->destination.type);
    add_assoc_bool(destination_arr, "queue_autoack", message->destination.queue_autoack);

    // fill origin array
    //char *orig_name = estrndup(message->origin.name, strlen(message->origin.name));
    array_init(origin_arr);
    //add_assoc_stringl(origin_arr, "name", dest_name, strlen(dest_name), 1);
    add_assoc_long(origin_arr, "type", message->origin.type);
    add_assoc_bool(origin_arr, "queue_autoack", message->origin.queue_autoack);

    // add both to message array
    add_assoc_zval(return_value, "origin", origin_arr);
    add_assoc_zval(return_value, "destination", destination_arr);
}

/* RECEIVE A MESSAGE_T WITH A TIMEOUT */
PHP_FUNCTION(broker_receive) {
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    long msecs;
    zval *zsapo_broker = NULL;
    zend_bool return_array;
    sapo_broker_t *sapo_broker;
    broker_msg_t *message;
    struct timeval timeout;

    if (zend_parse_parameters(argc TSRMLS_CC, "rl|b", &zsapo_broker, &msecs, &return_array) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);

    // convert miliseconds to timeval
    timeout.tv_sec = msecs / 1000;
    timeout.tv_usec = 1000 * (msecs % 1000);
    message = broker_receive(sapo_broker, &timeout);
    if (message == NULL)
        RETURN_FALSE;

    if (!return_array) {
        ZVAL_RES(return_value, zend_register_resource(message, le_broker_msg_t));
//        ZEND_REGISTER_RESOURCE(return_value, message, le_broker_msg_t);
    } else {
        char *payload = estrndup(message->payload, message->payload_len);
        char *message_id = estrndup(message->message_id, strlen(message->message_id));

        // alloc return array
        array_init(return_value);
        add_assoc_long(return_value, "payload_len", message->payload_len);
        add_assoc_stringl(return_value, "payload", payload, message->payload_len);
        add_assoc_stringl(return_value, "message_id", message_id, strlen(message->message_id));
        add_assoc_bool(return_value, "acked", message->acked);

        zval *destination_arr;
        zval *origin_arr;

        // again, thank you Zend for such a lame naming scheme. not mentioning CAPS, of course :)
        ALLOC_INIT_ZVAL(destination_arr);
        ALLOC_INIT_ZVAL(origin_arr);

        // fill destination array
        //char *dest_name = estrndup(message->destination.name, strlen(message->destination.name));
        array_init(destination_arr);
        //add_assoc_stringl(destination_arr, "name", dest_name, strlen(message->destination.name), 1);
        add_assoc_long(destination_arr, "type", message->destination.type);
        add_assoc_bool(destination_arr, "queue_autoack", message->destination.queue_autoack);

        // fill origin array
        //char *orig_name = estrndup(message->origin.name, strlen(message->origin.name));
        array_init(origin_arr);
        //add_assoc_stringl(origin_arr, "name", dest_name, strlen(dest_name), 1);
        add_assoc_long(origin_arr, "type", message->origin.type);
        add_assoc_bool(origin_arr, "queue_autoack", message->origin.queue_autoack);

        // add both to message array
        add_assoc_zval(return_value, "origin", origin_arr);
        add_assoc_zval(return_value, "destination", destination_arr);

        // returning by val. if we're losing our reference, we should auto-ack now
        // make sure this restriction (return by val only with auto-ack) is clear in docs
        // and filter above.
        // NOTE: broker_msg_ack calls broker_msg_free underneath
        broker_msg_ack(sapo_broker, message);
    }
}

/* SEND A MESSAGE TO A DESTINATION_T */
PHP_FUNCTION(broker_send) {
    zend_string *message = NULL;
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    int broker_destination_id = -1;
    int message_len;
    zval *sapo_broker = NULL;
    zval *broker_destination = NULL;

    if (zend_parse_parameters(argc TSRMLS_CC, "rrS", &sapo_broker, &broker_destination, &message ) == FAILURE)
        RETURN_FALSE;

    if (sapo_broker == NULL)
        RETURN_FALSE;

    //zend_fetch_resource(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);

    if (broker_destination == NULL)
        RETURN_FALSE;

    //zend_fetch_resource(???, ???, broker_destination, broker_destination_id, "???", ???_rsrc_id);
}

/* MULTIPURPOSE SUBSCRIBE A DESTINATION_T (QUEUE|TOPIC) */
PHP_FUNCTION(broker_subscribe) {
    int argc = ZEND_NUM_ARGS();
    int broker_handle_id = -1;
    int broker_destination_id = -1;
    zval *broker_handle = NULL;
    zval *broker_destination = NULL;

    if (zend_parse_parameters(argc TSRMLS_CC, "rr", &broker_handle, &broker_destination) == FAILURE)
        RETURN_FALSE;

    if (broker_handle) {
    //zend_fetch_resource(???, ???, broker_handle, broker_handle_id, "???", ???_rsrc_id);
    }
    if (broker_destination) {
    //zend_fetch_resource(???, ???, broker_destination, broker_destination_id, "???", ???_rsrc_id);
    }
}

/* BAREBONES (NO FANCY DESTINATION_T) SUBSCRIBE QUEUE */
PHP_FUNCTION(broker_subscribe_queue) {
    zend_string *queue = NULL;
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    int queue_len;
    zend_bool autoack;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rSb", &zsapo_broker, &queue, &autoack) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    char *internal_queue_name = estrndup(queue->val, queue->len);
    int retval = broker_subscribe_queue(sapo_broker, internal_queue_name, autoack);
    RETURN_LONG(retval);
}

/* BAREBONES (NO FANCY DESTINATION_T) SUBSCRIBE QUEUE */
PHP_FUNCTION(broker_subscribe_virtual_queue) {
    zend_string *queue = NULL;
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    int queue_len;
    zend_bool autoack;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rSb", &zsapo_broker, &queue, &autoack) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    char *internal_queue_name = estrndup(queue->val, queue->len);
    int retval = broker_subscribe_virtual_queue(sapo_broker, internal_queue_name, autoack);
    RETURN_LONG(retval);
}

/* BAREBONES (NO FANCY DESTINATION_T) SUBSCRIBE TOPIC */
PHP_FUNCTION(broker_subscribe_topic) {
    zend_string *topic = NULL;
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    int topic_len;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rS", &zsapo_broker, &topic) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    sapo_broker = (sapo_broker_t*)zend_fetch_resource_ex(zsapo_broker, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    char *internal_topic_name = estrndup(topic->val, topic->len);
    int retval = broker_subscribe_topic(sapo_broker, internal_topic_name);
    RETURN_LONG(retval);
}
