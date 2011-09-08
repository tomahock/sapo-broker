use Test::More;

BEGIN { use_ok('SAPO::Broker::Clients::Simple') }

use strict;
use warnings;

my $N    = $ENV{'BROKER_N_TESTS'} || 100;
my $host = $ENV{'BROKER_HOST'}    || 'broker.labs.sapo.pt';

sub read_name {
    my $rand_name;
    eval {
        open my $f, '<', '.broker_name' or die $!;
        $rand_name = <$f>;
        close($f) or die !$;;
    };
    if ($@) {
        warn $@;
        return 0;
    }

    return $rand_name;

}

sub rand_string($) {
    my ($n) = @_;

    my $ret = '';
    for ( 1 .. $n ) {
        $ret .= chr( int( rand(256) ) );
    }
    return $ret;
}

ok( my $name = read_name(), 'Read queue name' );
ok(
    my $broker = SAPO::Broker::Clients::Simple->new(
        'host'  => $host,
        'proto' => 'tcp'
    ),
    'Instantiate broker'
  );

my @payloads;

for my $n ( 1 .. $N ) {
    my $payload = rand_string( rand( $N / 2 ) );
    ok( defined $payload, 'Generate random payload' );
    push @payloads, $payload;
}

sub fill {
    my ($qname) = @_;
    $qname ||= $name;

    my %options = (
        'destination_type' => 'QUEUE',
        'destination'      => $qname,
    );

    my $n = 0;
    for my $payload (@payloads) {
        ++$n;
        ok( $broker->publish( %options, 'payload' => $payload ), "Published message $n" );
    }
}

fill();

ok(
    $broker->subscribe( (
            'destination_type' => 'QUEUE',
            'destination'      => $name,
            'auto_acknowledge' => 1
        )
    ),
    'Subscribe queue'
  );

my $n = 0;
for my $payload (@payloads) {
    ++$n;
    ok( my $message = $broker->receive(), "Receive subscribed message $n" );
    ok( $message->message->payload eq $payload, "Check paylod for message $n" );
}

my $pname = $name . "_poll";
fill($pname);

my %poptions = (
    'destination_type' => 'QUEUE',
    'destination'      => $pname,
);
$n = 0;
for my $payload (@payloads) {
    ++$n;
    ok( $broker->poll(%poptions), "Poll message $n" );
    ok( my $message = $broker->receive(), "Receive poll message $n" );
    ok( $broker->acknowledge($message), "Acknowledge poll message $n" );
    ok( $message->message->payload eq $payload, "Check paylod for message $n" );
}

done_testing( 4 + 9 * $N );

