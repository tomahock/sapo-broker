#!/usr/bin/env php
<?php
$skey = array_search("-b", $argv); $brokeraddr = $argv[$skey+1];
$pkey = array_search("-p", $argv); $brokerport = $argv[$pkey+1];
$broker = sapo_broker_init($brokeraddr, $brokerport, 0, 1);
$ret = sapo_broker_subscribe_queue($broker, "/test/foo", true);
$start = time();
for($i=0; $i<10000; $i++)
	$msg = sapo_broker_receive($broker, 1000);
sapo_broker_destroy($broker);
printf("Took %d seconds to consume 10k msgs\n", (time()-$start));
?>
