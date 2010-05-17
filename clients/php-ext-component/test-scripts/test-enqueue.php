#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", 3323, 0, 1);
$msg = "Hello, world!";
broker_enqueue($broker, "/test/foo", $msg, strlen($msg));
broker_destroy($broker);
?>
