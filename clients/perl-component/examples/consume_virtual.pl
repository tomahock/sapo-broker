use lib ('../lib');

use SAPO::Broker::Clients::Simple;
use Data::Dumper;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new(host=> 'broker.labs.sapo.pt');
my %options = (
	'destination_type' => 'VIRTUAL_QUEUE',
	'destination' => 'handle@topic',
	'auto_acknowledge' => 1
);

$broker->subscribe(%options);

my $N = $ARGV[0] || 100;

for my $n (1..$N){
	my $message = $broker->receive();
	print Dumper($message);
}
