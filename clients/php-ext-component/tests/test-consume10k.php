#!/usr/bin/env php
<?php
$skey = array_search("-b", $argv); $brokeraddr = $argv[$skey+1];
$pkey = array_search("-p", $argv); $brokerport = $argv[$pkey+1];
$qkey = array_search("-q", $argv); $brokerqueue = $argv[$qkey+1];

$broker = broker_init($brokeraddr, $brokerport, 0, 1);
$ret = broker_subscribe_queue($broker, $brokerqueue, 0);

$start = time();
$msgs = 0;

// consume without auto-ack
while (($msg = broker_receive($broker, 1000, true)) !== false) {
    echo "Got message: " . print_r($msg, true) . "\n";
    $msgs++;
}

broker_destroy($broker);
printf("Took %d seconds to consume %d msgs\n", (time()-$start), $msgs);
?>
