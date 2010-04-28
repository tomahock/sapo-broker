#!/usr/bin/env php
<?php
$skey = array_search("-b", $argv); $brokeraddr = $argv[$skey+1];
$pkey = array_search("-p", $argv); $brokerport = $argv[$pkey+1];
$qkey = array_search("-q", $argv); $brokerqueue = $argv[$qkey+1];
$broker = broker_init($brokeraddr, $brokerport, 0, 1);
$ret = broker_subscribe_queue($broker, $brokerqueue, true);
$start = time();
$msgs = 0;
do {
	$msg = broker_receive($broker, 1000);
	broker_msg_free($msg);
	$msgs++;
} while ($msg != NULL);

broker_destroy($broker);
printf("Took %d seconds to consume %d msgs\n", (time()-$start), $msgs-1);
?>
