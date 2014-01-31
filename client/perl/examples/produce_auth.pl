use lib ('../lib');

use SAPO::Broker::Clients::Simple;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new( 'proto' => 'ssl',
	host=> "broker.m3.bk.sapo.pt", port=> 3323 );

#$broker->authenticate( 'username', 'password' );

my %options = (
    'destination_type' => 'TOPIC', #'QUEUE',
    'destination'      => '/tests/perl/private',
);

my $N      = $ARGV[0] || 100;
my $prefix = $ARGV[1] || ( "time=" . time );

print "publishing...\n";

for my $n ( 1 .. $N ) {
    $broker->publish( %options, 'payload' => "$prefix\tpayload $n" );
}
