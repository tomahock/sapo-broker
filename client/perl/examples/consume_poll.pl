use lib ('../lib');

use SAPO::Broker::Clients::Simple;
use Data::Dumper;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new( 'host' => 'localhost' );
my %options = (
    'destination_type' => 'QUEUE',
    'destination'      => '/tests/perl',
    'auto_acknowledge' => 1
);

my $N = $ARGV[0] || 100;

for my $n ( 1 .. $N ) {
    $broker->poll(%options);
    my $notification = $broker->receive;
    my $payload      = $notification->message->payload;

    #print Dumper($notification);
}
