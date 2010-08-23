use Test::More;

BEGIN { use_ok('SAPO::Broker'); use_ok('SAPO::Broker::Clients::Simple') }

use strict;
use warnings;

my $name;
my $host;
my $N;
my @payloads;
my $broker;

sub read_info {
    eval {
        open my $f, '<:raw', '.broker_info' or die $!;
        local $/ = "\n";
        $name = <$f>;
        $host = <$f>;
        $N    = <$f>;
        chomp $name;
        chomp $host;
        chomp $N;
        close($f) or die !$;;
    };
    if ($@) {
        warn $@;
        return 0;
    } else {
        return 1;
    }
}

sub read_data {
    eval {
        open my $f, '<:raw', '.broker_data' or die $!;
        while ( my $length = <$f> ) {
            my $payload;
            read $f, $payload, $length;
            push @payloads, $payload;
        }
        close($f) or die !$;;
    };
    if ($@) {
        warn $@;
        return;
    } else {
        return 1;
    }
}

ok( read_info(), 'Read broker info' );
ok( read_data(), 'Read broker data' );

sub fill ($$) {
    my ( $queue_name, $proto ) = @_;
    my $qname = "$queue_name/$proto";

    my %options = (
        'destination_type' => 'QUEUE',
        'destination'      => $qname,
    );

    my $n = 0;
    for my $payload (@payloads) {
        ++$n;
        ok( $broker->publish( %options, 'payload' => $payload ), "Published message $n ($proto)" );
    }
}

sub test_queue($) {
    my $proto = shift;

    ok(
        $broker = SAPO::Broker::Clients::Simple->new(
            'host'  => $host,
            'proto' => lc($proto),
        ),
        'Instantiate broker'
      );

    my $qname = "$name/subscribe";
    fill( $qname, $proto );

    ok(
        $broker->subscribe( (
                'destination_type' => 'QUEUE',
                'destination'      => "$qname/$proto",
                'auto_acknowledge' => 1
            )
        ),
        "Subscribe queue ($proto)"
      );

    my $n = 0;
    for my $payload (@payloads) {
        ++$n;
        ok( my $message = $broker->receive(), "Receive subscribed message $n ($proto)" );
        ok( $message->message->payload eq $payload, "Check paylod for message $n ($proto)" );
    }

    my $pname = "$name/poll";
    fill( $pname, $proto );

    my %poptions = (
        'destination_type' => 'QUEUE',
        'destination'      => "$pname/$proto",
    );
    $n = 0;
    for my $payload (@payloads) {
        ++$n;
        ok( $broker->poll(%poptions), "Poll message $n ($proto)" );
        ok( my $message = $broker->receive(), "Receive poll message $n ($proto)" );
        ok( $broker->acknowledge($message), "Acknowledge poll message $n ($proto)" );
        ok( $message->message->payload eq $payload, "Check paylod for message $n ($proto)" );
    }
} ## end sub test_queue($)

test_queue('TCP');

#ssl stuff
SKIP: {
    if (SAPO::Broker::has_ssl) {
        test_queue('SSL');
    } else {
        skip "no SSL support", 6 + 8 * $N;
    }
}

done_testing( 4 + 2 * ( 2 + 8 * $N ) );
