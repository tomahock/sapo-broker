use lib ('../lib');

use SAPO::Broker::Clients::Async;
use AnyEvent;

use strict;
use warnings;

my $host = "10.135.66.175";    #"broker.labs.sapo.pt"; #'broker.m3.bk.sapo.pt'
my $port = 3390;

my $broker = SAPO::Broker::Clients::Async->new(
    host  => $host,
    port  => $port,
    codec => 'protobufxs',
    tls   => 1
);

my %options = (
    'destination_type' => 'TOPIC',         #'QUEUE',
    'destination'      => '/tests/perl',
);

my $N      = $ARGV[0] || 100;
my $prefix = $ARGV[1] || ( "time=" . time );

my $w = AnyEvent->condvar;

for my $n ( 1 .. $N ) {
    $broker->publish(
        %options,
        'payload' => "$prefix\tpayload $n",
        cb        => sub {

            #print "$n\n";
            $w->send if $n == $N;
        } );
}

$w->recv;

__END__


{
	timestamp: "yyyy-MM-dd hh:mm:ss.uuuu",
	component: "XXXXX",
	severity: "(INFO|WARNING|DEBUG|ERROR|FATAL)",
	description: "text"
}
