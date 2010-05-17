#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", 3323, TCP, PROTOBUF);
$msg = "Hello, world!";
broker_enqueue($broker, "/test/foo", $msg);
broker_destroy($broker);
?>
