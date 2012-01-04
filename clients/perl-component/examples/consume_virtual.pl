use lib ('../lib');

use SAPO::Broker::Clients::Simple;
use Data::Dumper;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new( host => 'broker.m3.bk.sapo.pt',port=>2222);
my %options = (
    'destination_type' => 'VIRTUAL_QUEUE',
    'destination'      => 'handle@/tests/perl',
    'auto_acknowledge' => 1
);

$broker->subscribe(%options);

my $N = $ARGV[0] || 100;

for my $n ( 1 .. $N ) {
    my $notification = $broker->receive();
    my $payload      = $notification->message->payload;

    #print Dumper($notification);
}
