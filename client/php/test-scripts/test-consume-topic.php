#!/usr/bin/env php
<?php

$broker = broker_init("192.168.100.1", SB_PORT, SB_TCP, SB_PROTOBUF);

$ret = broker_subscribe_topic($broker, "/test/foo");



// consume with auto-ack
while (1) {

	if (($msg = broker_receive($broker, 1000)) !== false){
	 	$msg_data = broker_msg_decode($msg);

    		echo "Got message: " . print_r($msg_data, true) . "\n";
	}else{
		var_dump($msg);	
		 echo broker_error($broker);
	}

}

?>
