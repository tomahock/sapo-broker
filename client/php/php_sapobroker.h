#ifndef PHP_SAPOBROKER_H
#define PHP_SAPOBROKER_H

extern zend_module_entry sapobroker_module_entry;
#define phpext_sapobroker_ptr &sapobroker_module_entry

PHP_MINIT_FUNCTION(sapobroker);
PHP_MSHUTDOWN_FUNCTION(sapobroker);
PHP_RINIT_FUNCTION(sapobroker);
PHP_RSHUTDOWN_FUNCTION(sapobroker);
PHP_MINFO_FUNCTION(sapobroker);

PHP_FUNCTION(broker_server_create);
PHP_FUNCTION(broker_init);
PHP_FUNCTION(broker_destroy);
PHP_FUNCTION(broker_add_server);
PHP_FUNCTION(broker_subscribe);
PHP_FUNCTION(broker_subscribe_topic);
PHP_FUNCTION(broker_subscribe_queue);
PHP_FUNCTION(broker_subscribe_virtual_queue);
PHP_FUNCTION(broker_receive);
PHP_FUNCTION(broker_msg_free);
PHP_FUNCTION(broker_msg_ack);
PHP_FUNCTION(broker_send);
PHP_FUNCTION(broker_publish);
PHP_FUNCTION(broker_enqueue);
PHP_FUNCTION(broker_error);
#endif

