<?php
$server = sapo_broker_server_create("127.0.0.1", 3323, 0, 1);
$broker = sapo_broker_init($server);
$msg = "Hello, world!";

for($i=0; $i<10000; $i++)
	sapo_broker_enqueue($broker, "/test/foo", $msg, strlen($msg));
?>