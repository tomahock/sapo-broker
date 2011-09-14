#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", SB_PORT, SB_TCP, SB_PROTOBUF);
$msg = "Hello, world!";
broker_publish($broker, "/test/foo", $msg);
broker_destroy($broker);
?>
