#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", SB_PORT, SB_UDP, SB_PROTOBUF);
$msg = "Hello, world!";
broker_enqueue($broker, "/test/foo", $msg);
broker_destroy($broker);
?>
