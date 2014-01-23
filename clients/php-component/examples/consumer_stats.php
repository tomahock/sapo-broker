#!/usr/bin/php -q
<?php
include('../classes/broker.php');
set_time_limit(0);
error_reporting(1);

#$broker=new SAPO_Broker(array('debug'=>TRUE));
$broker=new SAPO_Broker(array('server' => 'sm-encoder01.stormap.bk.sapo.pt'));

// consumer example
echo "Subscribing topics\n";
$broker->subscribe('/sapo/videos/flash_stats', array('destination_type' => 'QUEUE'),"processStats");

echo "Entering consumer() loop now\n";
$broker->consumer();

echo "Consumer exited (last err: ".$broker->net->last_err.")\n";

function processStats($payload) {
  echo "processStats: ".$payload."\n";
  }

?>
