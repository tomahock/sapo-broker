#!/usr/bin/env php
<?php

// instantiate the broker handle
$broker = broker_init("127.0.0.1", 3323, SB_TCP, SB_PROTOBUF);
$msg = "Hello, world!";

// publish
$ret = broker_publish($broker, "/test/foo", $msg);

var_dump($ret);

// dealloc broker
broker_destroy($broker);
?>
