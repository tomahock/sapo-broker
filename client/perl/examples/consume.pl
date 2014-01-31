use lib ('../lib');

use SAPO::Broker::Clients::Simple;
use Data::Dumper;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new(
    'host'  => 'localhost',
    'proto' => 'tcp'
);
my %options = (
    'destination_type' => 'TOPIC',
    'destination'      => '/tests/perl',
    'auto_acknowledge' => 1
);

$broker->subscribe(%options);

my $N = $ARGV[0] || 100;

for my $n ( 1 .. $N ) {
    my $notification = $broker->receive;
    my $payload      = $notification->message->payload;
    print $payload. "\n";
}
