#!/usr/bin/env php
<?php

$broker = broker_init("127.0.0.1", 3323, 0, 1);
$result = broker_add_server($broker, "localhost", 3323, 0, 1);
if ($result === false)
    die("broker_add_server failed");

$msg = "Hello, world!";
$start = time();
for($i=0; $i<10000; $i++) {
	broker_enqueue($broker, "/test/foo", $msg, strlen($msg));
}
printf("Took %d seconds to produce 10k msgs\n", (time()-$start));
broker_destroy($broker);
?>
