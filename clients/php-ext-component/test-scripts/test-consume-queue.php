#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", SB_PORT, SB_TCP, SB_PROTOBUF);
$ret = broker_subscribe_queue($broker, "/test/foo", 0);

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
