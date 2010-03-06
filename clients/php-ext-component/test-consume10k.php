<?php
$server = sapo_broker_server_create("127.0.0.1", 3323, 0, 1);
$broker = sapo_broker_init($server);
$ret = sapo_broker_subscribe_queue($broker, "/test/foo", true);
echo $ret."\n";

for($i=0; $i<10000; $i++)
	$msg = sapo_broker_receive($broker, 10);
?>