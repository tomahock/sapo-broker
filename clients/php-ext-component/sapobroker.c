#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "php.h"
#include "php_ini.h"
#include "ext/standard/info.h"
#include "php_sapobroker.h"

#include "/Volumes/SVN Repo/broker/trunk/clients/c-component/libsapo-broker2/src/sapo_broker.h"

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

/* {{{ proto resource broker_init(resource broker_server)
   Returns a broker handle */
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
/* }}} */

/* {{{ proto int broker_enqueue(resource sapo_broker, string queue, string message, int message_size)
   Push message to queue */
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

	if (sapo_broker) {
		ZEND_FETCH_RESOURCE(sapo_broker, sapo_broker_t*, &zsapo_broker, -1, PHP_SAPO_BROKER_T_RES_NAME, le_sapo_broker_t);
		//php_printf("Will publish %s to queue %s with %d hosts\n", message, queue, sapo_broker->servers.server_count);
		ret = broker_enqueue(sapo_broker, queue, message, message_len);
		//php_printf("Result was: %d\n", ret);
	}
}
/* }}} */

/* {{{ proto int broker_destroy(resource broker_handle)
   Destroys a broker handle */
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
/* }}} */

/* {{{ proto int broker_add_server(resource broker_handle, resource broker_server)
   Adds a broker server to a broker handle */
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
/* }}} */

/* {{{ proto int broker_subscribe(resource broker_handle, resource broker_destination)
   Subscribe a destination on a broker handle */
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
/* }}} */

/* {{{ proto int broker_subscribe_topic(resource sapo_broker, string topic)
   Subscribe a topic on a broker handle */
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
/* }}} */

/* {{{ proto int broker_subscribe_queue(resource sapo_broker, string queue, boolean autoack)
   Subscribe a queue on a broker handle */
PHP_FUNCTION(sapo_broker_subscribe_queue) {
	char *queue = NULL;
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	int queue_len;
	zend_bool autoack;
	zval *sapo_broker = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "rsb", &sapo_broker, &queue, &queue_len, &autoack) == FAILURE) 
		return;

	if (sapo_broker) {
		//ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
	}

}
/* }}} */

/* {{{ proto resource broker_receive(resource sapo_broker, int msecs)
   Receive from broker with timeout in milisecs */
PHP_FUNCTION(sapo_broker_receive) {
	int argc = ZEND_NUM_ARGS();
	int sapo_broker_id = -1;
	long msecs;
	zval *sapo_broker = NULL;

	if (zend_parse_parameters(argc TSRMLS_CC, "rl", &sapo_broker, &msecs) == FAILURE) 
		return;

	if (sapo_broker) {
		//ZEND_FETCH_RESOURCE(???, ???, sapo_broker, sapo_broker_id, "???", ???_rsrc_id);
	}

}
/* }}} */

/* {{{ proto int broker_msg_free(resource message)
   Free a message resource */
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
/* }}} */

/* {{{ proto int broker_msg_ack(resource sapo_broker)
   message) */
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
/* }}} */

/* {{{ proto int broker_send(resource sapo_broker, resource broker_destination, string message, int message_size)
   Send a message to a destination */
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
/* }}} */

/* {{{ proto string broker_error(resource sapo_broker)
   Retrieve last error from broker handle */
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
/* }}} */
