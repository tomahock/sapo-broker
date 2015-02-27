use lib ('../lib');

use SAPO::Broker::Clients::Simple;
use Time::HiRes qw(usleep nanosleep);

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new(
    'host'  => 'localhost',
    'proto' => 'tcp'
);

my %options = (
    'destination_type' => 'QUEUE', #'QUEUE',
    'destination'      => '/tests/perlclient',
);

my $produced_messages = 0;

my $prefix = ( "time=" . time );

while(1){
    $prefix = ( "time=" . time );
    $broker->publish( %options, 'payload' => "$prefix\tpayload" );
    $produced_messages++;
    if($produced_messages % 100 == 0){
      print "Sleeping for 20 minutes after payload sent: " . "$prefix\tpayload" . "\n";
      sleep(1200) #sleep for 20 minutes
    } else {
      sleep(1);
    }
}
