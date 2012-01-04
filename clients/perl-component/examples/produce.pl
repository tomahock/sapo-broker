use lib ('../lib');

use SAPO::Broker::Clients::Simple;

use strict;
use warnings;

my $broker  = SAPO::Broker::Clients::Simple->new(host=>'broker.m3.bk.sapo.pt',port=>2222);
my %options = (
    'destination_type' => 'QUEUE',
    'destination'      => '/tests/perl',
);

my $N      = $ARGV[0] || 100;
my $prefix = $ARGV[1] || ( "time=" . time );

for my $n ( 1 .. $N ) {
    $broker->publish( %options, 'payload' => "$prefix\tpayload $n" );
}
