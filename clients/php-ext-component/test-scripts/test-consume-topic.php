#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", 3323, TCP, PROTOBUF);
$ret = broker_subscribe_topic($broker, "/test/foo");

// consume without auto-ack
while (1) {
	if (($msg = broker_receive($broker, 1000, true)) !== false)
    	echo "Got message: " . print_r($msg, true) . "\n";
}

?>
