use lib ('../lib');

use SAPO::Broker::Clients::Simple;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new( host => 'broker.m3.bk.sapo.pt', );

my $action_id = int( rand(1000000000) );
print("sending ping [$action_id]\n");
$broker->ping($action_id);
print("sent\n");
print("waiting\n");
my $msg           = $broker->receive();
my $action_id_new = $msg->{'action_id'};
print("received pong [$action_id_new]\n");
print( $action_id_new == $action_id ? 'OK' : 'ERROR' );
print("\n");
