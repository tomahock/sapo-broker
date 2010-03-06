#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "php.h"
#include "php_ini.h"
#include "ext/standard/info.h"
#include "php_sapobroker.h"

#include "../c-component/libsapo-broker2/src/sapo_broker.h"

#define PHP_BROKER_SERVER_T_RES_NAME "Broker Server"
#define PHP_SAPO_BROKER_T_RES_NAME "Sapo Broker"

static int le_sapobroker;
int le_broker_server_t;
int le_sapo_broker_t;

const zend_function_entry sapobroker_functions[] = {
	PHP_FE(sapo_broker_server_create, NULL)
	PHP_FE(sapo_broker_init, NULL)
	PHP_FE(sapo_broker_destroy, NULL)
	PHP_FE(sapo_broker_add_server, NULL)
	PHP_FE(sapo_broker_subscribe, NULL)
	PHP_FE(sapo_broker_subscribe_topic, NULL)
	PHP_FE(sapo_broker_subscribe_queue, NULL)
	PHP_FE(sapo_broker_receive, NULL)
	PHP_FE(sapo_broker_msg_free, NULL)
	PHP_FE(sapo_broker_msg_ack, NULL)
	PHP_FE(sapo_broker_send, NULL)
	PHP_FE(sapo_broker_enqueue, NULL)
	PHP_FE(sapo_broker_error, NULL)
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
	NULL,//PHP_RINIT(sapobroker), * Replace with NULL if there's nothing to do at request start */
	NULL,//PHP_RSHUTDOWN(sapobroker), /* Replace with NULL if there's nothing to do at request end */
	PHP_MINFO(sapobroker),
#if ZEND_MODULE_API_NO >= 20010901
	"0.1", /* Replace with version number for your extension */
#endif
	STANDARD_MODULE_PROPERTIES
};

#ifdef COMPILE_DL_SAPOBROKER
ZEND_GET_MODULE(sapobroker)
#endif

PHP_MINIT_FUNCTION(sapobroker) {
	le_broker_server_t = zend_register_list_destructors_ex(NULL, NULL, PHP_BROKER_SERVER_T_RES_NAME, module_number);
	le_sapo_broker_t = zend_register_list_destructors_ex(NULL, NULL, PHP_SAPO_BROKER_T_RES_NAME, module_number);
	return SUCCESS;
}

PHP_MSHUTDOWN_FUNCTION(sapobroker) {return SUCCESS;}
PHP_RINIT_FUNCTION(sapobroker) {return SUCCESS;}
PHP_RSHUTDOWN_FUNCTION(sapobroker) {return SUCCESS;}
PHP_MINFO_FUNCTION(sapobroker) {
	php_info_print_table_start();
	php_info_print_table_header(2, "sapobroker support", "enabled");
	php_info_print_table_end();
}

/* ADDITIONAL FUNCTION TO INITIALIZE A BROKER_SERVER_T */
PHP_FUNCTION(sapo_broker_server_create) {
	int argc = ZEND_NUM_ARGS();
	char *hostname;
	uint16_t port, namelen;
	uint8_t transport, protocol;
	if (zend_parse_parameters(argc TSRMLS_CC, "slll", &hostname, &namelen, &port, &transport, &protocol) == FAILURE)
		RETURN_FALSE;

	// alloc this or die
	broker_server_t *output = emalloc(sizeof(broker_server_t));
	output->hostname = estrndup(hostname, namelen);
	output->port = port;
	output->transport = transport;
	output->protocol = protocol;
	
	ZEND_REGISTER_RESOURCE(return_value, output, le_broker_server_t);
}

/* INITIALIZE A BROKER HANDLE FROM BROKER SERVER_T */
PHP_FUNCTION(sapo_broker_init) {
	int argc = ZEND_NUM_ARGS();
	zval *zbroker_server = NULL;

	broker_server_t *broker_server;
	sapo_broker_t *anewbroker;

	if (zend_parse_parameters(argc TSRMLS_CC, "r", &zbroker_server) == FAILURE) 
		RETURN_FALSE;

	if (broker_server) {
		// fetch broker_server parameter
		ZEND_FETCH_RESOURCE(broker_server, broker_server_t*, &zbroker_server, -1, PHP_BROKER_SERVER_T_RES_NAME, le_broker_server_t);
		//php_printf("Got broker server: %s:%d\n", broker_server->hostname, broker_server->port);
		
		// call libsapobroker broker_init
		anewbroker = broker_init(*broker_server);
		
		// register the result as a resource in the le table and return it
		ZEND_REGISTER_RESOURCE(return_value, anewbroker, le_sapo_broker_t);
	}
}

/* ENQUEUE A MESSAGE */
PHP_FUNCTION(sapo_broker_enqueue) {
	char *queue = NULL;
	char *message = NULL;
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	int queue_len;
	int message_len;
	long message_size;
	zval *zsapo_broker = NULL;
	int ret;
	sapo_broker_t *sapo_broker;

	if (zend_parse_parameters(argc TSRMLS_CC, "rssl", &zsapo_broker, &queue, &queue_len, &message, &message_len, &message_size) == FAILURE) 
		RETURN_FALSE;

	if (zsapo_broker) {
		ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
		//php_printf("Will publish %s to queue %s with %d hosts\n", message, queue, sapo_broker->servers.server_count);
		ret = broker_enqueue(sapo_broker, queue, message, message_len);
		//php_printf("Result was: %d\n", ret);
	}
}

/* DEALLOC A BROKER HANDLE */
PHP_FUNCTION(sapo_broker_destroy) {
	int argc = ZEND_NUM_ARGS();
	int broker_handle_id = -1;
	zval *broker_handle = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "r", &broker_handle) == FAILURE) 
		return;

	if (broker_handle) {
		//ZEND_FETCH_RESOURCE(???, ???, broker_handle, broker_handle_id, "???", ???_rsrc_id);
	}
}

/* ADD BROKER SERVER_T TO BROKER HANDLE_T */
PHP_FUNCTION(sapo_broker_add_server) {
	int argc = ZEND_NUM_ARGS();
	int broker_handle_id = -1;
	int broker_server_id = -1;
	zval *broker_handle = NULL;
	zval *broker_server = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "rr", &broker_handle, &broker_server) == FAILURE) 
		return;

	if (broker_handle) {
		//ZEND_FETCH_RESOURCE(???, ???, broker_handle, broker_handle_id, "???", ???_rsrc_id);
	}
	if (broker_server) {
		//ZEND_FETCH_RESOURCE(???, ???, broker_server, broker_server_id, "???", ???_rsrc_id);
	}
}

/* SUBSCRIBE A DESTINATION_T (QUEUE|TOPIC) */
PHP_FUNCTION(sapo_broker_subscribe) {
	int argc = ZEND_NUM_ARGS();
	int broker_handle_id = -1;
	int broker_destination_id = -1;
	zval *broker_handle = NULL;
	zval *broker_destination = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "rr", &broker_handle, &broker_destination) == FAILURE) 
		return;

	if (broker_handle) {
		//ZEND_FETCH_RESOURCE(???, ???, broker_handle, broker_handle_id, "???", ???_rsrc_id);
	}
	if (broker_destination) {
		//ZEND_FETCH_RESOURCE(???, ???, broker_destination, broker_destination_id, "???", ???_rsrc_id);
	}
}

/* BAREBONES (NO FANCY DESTINATION_T) SUBSCRIBE TOPIC */
PHP_FUNCTION(sapo_broker_subscribe_topic) {
	char *topic = NULL;
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	int topic_len;
	zval *sapo_broker = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "rs", &sapo_broker, &topic, &topic_len) == FAILURE) 
		return;

	if (sapo_broker) {
		//ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
	}
}

/* BAREBONES (NO FANCY DESTINATION_T) SUBSCRIBE QUEUE */
PHP_FUNCTION(sapo_broker_subscribe_queue) {
	char *queue = NULL;
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	int queue_len;
	zend_bool autoack;
	zval *zsapo_broker = NULL;
	sapo_broker_t *sapo_broker;
	char *internal_queue_name;

	if (zend_parse_parameters(argc TSRMLS_CC, "rsb", &zsapo_broker, &queue, &queue_len, &autoack) == FAILURE) 
		RETURN_FALSE;

	if (zsapo_broker) {
		ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
		internal_queue_name = estrndup(queue, queue_len);
		return_value = broker_subscribe_queue(sapo_broker, internal_queue_name, autoack);
	}
}

/* RECEIVE A MESSAGE_T WITH A TIMEOUT */
PHP_FUNCTION(sapo_broker_receive) {
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	long msecs;
	zval *zsapo_broker = NULL;
	sapo_broker_t *sapo_broker;
	broker_msg_t *message;
	struct timeval timeout;
	char *payload, *message_id;

	if (zend_parse_parameters(argc TSRMLS_CC, "rl", &zsapo_broker, &msecs) == FAILURE)
		RETURN_FALSE;

	if (zsapo_broker) {
		ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
		// convert miliseconds to timeval
		timeout.tv_sec = msecs/1000;
		timeout.tv_usec = 1000 * (msecs%1000);
		message = broker_receive(sapo_broker, &timeout);
		if (message == NULL)
			RETURN_FALSE;
		
		array_init(return_value);
		add_assoc_long(return_value, "payload_length", message->payload_len);
		
		payload = estrndup(message->payload, message->payload_len);
		add_assoc_stringl(return_value, "payload", payload, message->payload_len, 1);
		
		message_id = estrndup(message->message_id, strlen(message->message_id));
		add_assoc_stringl(return_value, "message_id", message_id, strlen(message_id), 1);
		
		// note: the lib needs to be called in order to dealloc a message_t.
		// we either explicitly request a dealloc here or we store a ref and postpone the request
	}
}

/* FREE A MESSAGE_T */
PHP_FUNCTION(sapo_broker_msg_free) {
	int argc = ZEND_NUM_ARGS();
	int message_id = -1;
	zval *message = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "r", &message) == FAILURE) 
		return;

	if (message) {
		//ZEND_FETCH_RESOURCE(???, ???, message, message_id, "???", ???_rsrc_id);
	}

}

/* ACK A MESSAGE */
PHP_FUNCTION(sapo_broker_msg_ack) {
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	zval *sapo_broker = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "r", &sapo_broker) == FAILURE) 
		return;

	if (sapo_broker) {
		//ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
	}

}

/* SEND A MESSAGE TO A DESTINATION_T */
PHP_FUNCTION(sapo_broker_send) {
	char *message = NULL;
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	int broker_destination_id = -1;
	int message_len;
	long message_size;
	zval *sapo_broker = NULL;
	zval *broker_destination = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "rrsl", &sapo_broker, &broker_destination, &message, &message_len, &message_size) == FAILURE) 
		return;

	if (sapo_broker) {
		//ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
	}
	if (broker_destination) {
		//ZEND_FETCH_RESOURCE(???, ???, broker_destination, broker_destination_id, "???", ???_rsrc_id);
	}

}

/* GET LAST ERROR */
PHP_FUNCTION(sapo_broker_error) {
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	zval *sapo_broker = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "r", &sapo_broker) == FAILURE) 
		return;

	if (sapo_broker) {
		//ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
	}

}
