use lib ('../lib');

use SAPO::Broker::Clients::Async;
use Data::Dumper;
use AnyEvent;

use strict;
use warnings;

my $host = "10.135.66.175";    #"broker.labs.sapo.pt"; #'broker.m3.bk.sapo.pt'
my $port = 3390;

my $broker = SAPO::Broker::Clients::Async->new(
    host  => $host,
    port  => $port,
    codec => 'protobufxs',
    tls   => 1,
    rcb   => sub {
        my ($msg) = @_;
        my $payload = $msg->message->payload;
        print STDERR "[$payload]\n";
    } );

my %options = (

    #    'destination_type' => 'VIRTUAL_QUEUE', #'TOPIC',
    #    'destination'      => 'test@/tests/perl',
    'destination_type' => 'TOPIC',
    'destination'      => '/tests/perl',
    'auto_acknowledge' => 1,
);

$broker->subscribe(%options);

my $w = AnyEvent->condvar;
$w->recv;
