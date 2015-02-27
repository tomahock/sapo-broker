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
    'destination_type' => 'QUEUE',
    'destination'      => '/tests/perlclient',
    'auto_acknowledge' => 1
);

$broker->subscribe(%options);

while(1) {
    my $notification = $broker->receive;
    my $payload      = $notification->message->payload;
    print $payload. "\n";
}
