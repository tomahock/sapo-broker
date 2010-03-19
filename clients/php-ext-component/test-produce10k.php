#!/usr/bin/env php
<?php
$broker = sapo_broker_init("127.0.0.1", 3323, 0, 1);
$msg = "Hello, world!";
$start = time();
for($i=0; $i<10000; $i++)
	sapo_broker_enqueue($broker, "/test/foo", $msg, strlen($msg));
printf("Took %d seconds to produce 10k msgs\n", (time()-$start));
sapo_broker_destroy($broker);
?>
