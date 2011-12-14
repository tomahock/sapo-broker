#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", SB_PORT, SB_TCP, SB_PROTOBUF);
$ret = broker_subscribe_virtual_queue($broker, "prefix@/test/foo", 0);

$start = time();
$msgs = 0;

// consume without auto-ack
while (1) {
    $msg = broker_receive($broker, 1000, true);
    echo "Got message: " . print_r($msg, true) . "\n";
    $msgs++;
}

// never reached
broker_destroy($broker);
printf("Took %d seconds to consume %d msgs\n", (time()-$start), $msgs);
?>
