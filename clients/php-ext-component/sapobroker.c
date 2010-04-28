#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "php.h"
#include "php_ini.h"
#include "ext/standard/info.h"
#include "php_sapobroker.h"

#include "../c-component/libsapo-broker2/src/libsapo-broker2.h"

#define PHP_BROKER_SERVER_T_RES_NAME "Broker Server"
#define PHP_SAPO_BROKER_T_RES_NAME "Sapo Broker"
#define PHP_BROKER_MSG_T_RES_NAME "Sapo Broker Message"
#define PHP_SAPO_BROKER_EXTENSION_VERSION "0.2"

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
    PHP_FE(broker_receive, NULL)
    PHP_FE(broker_msg_free, NULL)
    PHP_FE(broker_msg_ack, NULL)
    PHP_FE(broker_send, NULL)
    PHP_FE(broker_publish, NULL)
    PHP_FE(broker_enqueue, NULL)
    PHP_FE(broker_error, NULL)
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
    char *hostname;
    uint16_t port, namelen;
    uint8_t transport, protocol;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rslll", &zsapo_broker, &hostname, &namelen, &port, &transport, &protocol) == FAILURE) 
        RETURN_FALSE;

    if (sapo_broker == NULL)
        RETURN_FALSE;

    ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);

    // alloc new server_t, copy and pass to libsapobroker
    broker_server_t *server = emalloc(sizeof(broker_server_t));
    server->hostname = estrndup(hostname, namelen);
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

    ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    int retval = broker_destroy(sapo_broker);
    zend_list_delete(Z_LVAL_P(zsapo_broker));

    RETURN_TRUE;
}

/* ENQUEUE A MESSAGE */
PHP_FUNCTION(broker_enqueue) {
    char *queue = NULL;
    char *message = NULL;
    int argc = ZEND_NUM_ARGS();
    int queue_len;
    int message_len;
    long message_size;
    zval *zsapo_broker = NULL;
    int retval;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rssl", &zsapo_broker, &queue, &queue_len, &message, &message_len, &message_size) == FAILURE) 
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    retval = broker_enqueue(sapo_broker, queue, message, message_len);
    RETURN_LONG(retval);
}

/* GET LAST ERROR */
PHP_FUNCTION(broker_error) {
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    zval *zsapo_broker = NULL;

    if (zend_parse_parameters(argc TSRMLS_CC, "r", &sapo_broker) == FAILURE) 
        RETURN_FALSE;

    if (sapo_broker == NULL)
        RETURN_FALSE;

    ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    char *msg = broker_error(sapo_broker);
    
    // return without allocating a copy. as long as sapo_broker is alive, so is the string
    RETURN_STRING(msg, 0);
}

/* ADDITIONAL FUNCTION TO INITIALIZE A BROKER_SERVER_T */
PHP_FUNCTION(broker_init) {
    int argc = ZEND_NUM_ARGS();
    char *hostname;
    uint16_t port, namelen;
    uint8_t transport, protocol;

    if (zend_parse_parameters(argc TSRMLS_CC, "slll", &hostname, &namelen, &port, &transport, &protocol) == FAILURE)
        RETURN_FALSE;

    // alloc this or die
    broker_server_t *server = emalloc(sizeof(broker_server_t));
    server->hostname = estrndup(hostname, namelen);
    server->port = port;
    server->transport = transport;
    server->protocol = protocol;
    sapo_broker_t *abroker = broker_init(*server);

    ZEND_REGISTER_RESOURCE(return_value, abroker, le_sapo_broker_t);
}

/* ACK A MESSAGE */
PHP_FUNCTION(broker_msg_ack) {
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    zval *sapo_broker = NULL;

    if (zend_parse_parameters(argc TSRMLS_CC, "r", &sapo_broker) == FAILURE) 
        return;

    if (sapo_broker) {
    //ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
    }
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

    //ZEND_FETCH_RESOURCE(???, ???, message, message_id, "???", ???_rsrc_id);
}

/* PUBLISH TO TOPIC */
PHP_FUNCTION(broker_publish) {
    char *topic = NULL;
    char *message = NULL;
    int argc = ZEND_NUM_ARGS();
    int topic_len;
    int message_len;
    long message_size;
    zval *zsapo_broker = NULL;
    int retval;
    sapo_broker_t *sapo_broker;

    if (zend_parse_parameters(argc TSRMLS_CC, "rssl", &zsapo_broker, &topic, &topic_len, &message, &message_len, &message_size) == FAILURE) 
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    retval = broker_publish(sapo_broker, topic, message, message_len);
    RETURN_LONG(retval);
}

/* RECEIVE A MESSAGE_T WITH A TIMEOUT */
PHP_FUNCTION(broker_receive) {
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    long msecs;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;
    broker_msg_t *message;
    struct timeval timeout;

    if (zend_parse_parameters(argc TSRMLS_CC, "rl", &zsapo_broker, &msecs) == FAILURE)
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    // convert miliseconds to timeval
    timeout.tv_sec = msecs/1000;
    timeout.tv_usec = 1000 * (msecs%1000);
    message = broker_receive(sapo_broker, &timeout);
    if (message == NULL)
        RETURN_FALSE;

    // needs something the likes of sapo_broker_fetch_msg($msg_resource) : array as key => val
    ZEND_REGISTER_RESOURCE(return_value, message, le_broker_msg_t);
}

/* SEND A MESSAGE TO A DESTINATION_T */
PHP_FUNCTION(broker_send) {
    char *message = NULL;
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    int broker_destination_id = -1;
    int message_len;
    long message_size;
    zval *sapo_broker = NULL;
    zval *broker_destination = NULL;

    if (zend_parse_parameters(argc TSRMLS_CC, "rrsl", &sapo_broker, &broker_destination, &message, &message_len, &message_size) == FAILURE) 
        RETURN_FALSE;

    if (sapo_broker == NULL)
        RETURN_FALSE;
    
    //ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
    
    if (broker_destination == NULL)
        RETURN_FALSE;

    //ZEND_FETCH_RESOURCE(???, ???, broker_destination, broker_destination_id, "???", ???_rsrc_id);
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
    //ZEND_FETCH_RESOURCE(???, ???, broker_handle, broker_handle_id, "???", ???_rsrc_id);
    }
    if (broker_destination) {
    //ZEND_FETCH_RESOURCE(???, ???, broker_destination, broker_destination_id, "???", ???_rsrc_id);
    }
}

/* BAREBONES (NO FANCY DESTINATION_T) SUBSCRIBE QUEUE */
PHP_FUNCTION(broker_subscribe_queue) {
    char *queue = NULL;
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    int queue_len;
    zend_bool autoack;
    zval *zsapo_broker = NULL;
    sapo_broker_t *sapo_broker;
    char *internal_queue_name;
    int retval;

    if (zend_parse_parameters(argc TSRMLS_CC, "rsb", &zsapo_broker, &queue, &queue_len, &autoack) == FAILURE) 
        RETURN_FALSE;

    if (zsapo_broker == NULL)
        RETURN_FALSE;

    ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
    internal_queue_name = estrndup(queue, queue_len);
    retval = broker_subscribe_queue(sapo_broker, internal_queue_name, autoack);
    RETURN_LONG(retval);
}

/* BAREBONES (NO FANCY DESTINATION_T) SUBSCRIBE TOPIC */
PHP_FUNCTION(broker_subscribe_topic) {
    char *topic = NULL;
    int argc = ZEND_NUM_ARGS();
    int sapo_broker_id = -1;
    int topic_len;
    zval *sapo_broker = NULL;

    if (zend_parse_parameters(argc TSRMLS_CC, "rs", &sapo_broker, &topic, &topic_len) == FAILURE) 
        RETURN_FALSE;

    if (sapo_broker == NULL)
        RETURN_FALSE;

    //ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
}
