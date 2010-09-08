use lib ('../lib');

use SAPO::Broker::Clients::Simple;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new('proto' => 'ssl');

$broker->authenticate('username', 'password');

my %options = (
	'destination_type' => 'QUEUE',
	'destination' => '/tests/perl/private',
);

my $N = $ARGV[0] || 100;
my $prefix = $ARGV[1] || ("time=".time);

for my $n (1..$N){
	$broker->publish(%options, 'payload' => "$prefix\tpayload $n");
}
