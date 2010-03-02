#ifndef PHP_SAPOBROKER_H
#define PHP_SAPOBROKER_H

extern zend_module_entry sapobroker_module_entry;
#define phpext_sapobroker_ptr &sapobroker_module_entry

PHP_MINIT_FUNCTION(sapobroker);
PHP_MSHUTDOWN_FUNCTION(sapobroker);
PHP_RINIT_FUNCTION(sapobroker);
PHP_RSHUTDOWN_FUNCTION(sapobroker);
PHP_MINFO_FUNCTION(sapobroker);

PHP_FUNCTION(sapo_broker_server_create);
PHP_FUNCTION(sapo_broker_init);
PHP_FUNCTION(sapo_broker_destroy);
PHP_FUNCTION(sapo_broker_add_server);
PHP_FUNCTION(sapo_broker_subscribe);
PHP_FUNCTION(sapo_broker_subscribe_topic);
PHP_FUNCTION(sapo_broker_subscribe_queue);
PHP_FUNCTION(sapo_broker_receive);
PHP_FUNCTION(sapo_broker_msg_free);
PHP_FUNCTION(sapo_broker_msg_ack);
PHP_FUNCTION(sapo_broker_send);
PHP_FUNCTION(sapo_broker_enqueue);
PHP_FUNCTION(sapo_broker_enqueue);
PHP_FUNCTION(sapo_broker_error);
#endif

