use Test::More qw(no_plan);

use_ok('SAPO::Broker::Utils');
use_ok('SAPO::Broker::Clients::Simple');

my $thrift;

SKIP: {
    if ( SAPO::Broker::Utils::has_thrift() ) {
        use_ok('SAPO::Broker::Codecs::Thrift');
        ok( $thrift = SAPO::Broker::Codecs::Thrift->new(), 'Thrift codec' );
    } else {
        skip( 'no thrift support', 2 );
    }
}

my $thriftxs;

SKIP: {
    if ( SAPO::Broker::Utils::has_thriftxs() ) {
        use_ok('SAPO::Broker::Codecs::Thrift');
        ok( $thriftxs = SAPO::Broker::Codecs::Thrift->new(), 'ThriftXS codec' );
    } else {
        skip( 'no thriftxs support', 2 );
    }
}

my $protobuf;

SKIP: {
    if ( SAPO::Broker::Utils::has_protobufxs() ) {
        use_ok('SAPO::Broker::Codecs::ProtobufXS');
        ok( $protobuf = SAPO::Broker::Codecs::ProtobufXS->new(), 'ProtobufXS codec' );
    } else {
        skip( 'no protobufxs support', 2 );
    }
}

use strict;
use warnings;

my $name;
my $host;
my $N;
my $action_id;
my @payloads;

sub read_info {
    eval {
        open my $f, '<:raw', '.broker_info' or die $!;
        local $/ = "\n";
        $name = <$f>;
        $host = <$f>;
        $N    = <$f>;
		$action_id = <$f>;
        chomp $name;
        chomp $host;
        chomp $N;
		chomp $action_id;
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

#plan tests => 30 + 64 * $N;

ok( read_data(), 'Read broker data' );

sub fill ($$$;$) {
    my ( $queue_name, $proto, $kind, $brk ) = @_;
    my $qname = "$queue_name/$proto";

    my %options = (
        'destination_type' => $kind,
        'destination'      => $qname,
    );

    my $n = 0;
    for my $payload (@payloads) {
        ++$n;
        ok( $brk->publish( %options, 'payload' => $payload ), "Published message $n ($proto)" );
    }
}

sub test_queue($;$$) {
    my ( $proto, $pproto, $codec ) = @_;

    $pproto ||= $proto;
    $codec  ||= $thrift;
    my $codec_name = ref($codec) ? $codec->name : $codec;

    ok(
        my $broker = SAPO::Broker::Clients::Simple->new(
            'codec'   => $codec,
            'host'    => $host,
            'proto'   => lc($pproto),
            'timeout' => 10,
        ),
        "Instantiate broker ($pproto)"
      );

    my $qname = "$name/subscribe";
    fill( $qname, $pproto, 'QUEUE', $broker );

    if ( $pproto ne $proto ) {
        ok(
            $broker = SAPO::Broker::Clients::Simple->new(
                'codec'   => $codec,
                'host'    => $host,
                'proto'   => lc($proto),
                'timeout' => 10,
            ),
            "Instantiate broker ($proto)"
          );

    }

    ok(
        $broker->subscribe( (
                'destination_type' => 'QUEUE',
                'destination'      => "$qname/$pproto",
                'auto_acknowledge' => 1
            )
        ),
        "Subscribe queue ($proto)"
      );

    my $n = 0;
    for my $payload (@payloads) {
        ++$n;
        ok( my $message = $broker->receive(), "Receive subscribed message $n ($proto) [$codec_name]" );
        ok( $message->message->payload eq $payload, "Check paylod for message $n ($proto) [$codec_name]" );
    }

    my $pname = "$name/poll";
    fill( $pname, $pproto, 'QUEUE', $broker );

    my %poptions = (
        'destination_type' => 'QUEUE',
        'destination'      => "$pname/$pproto",
    );
    $n = 0;
    for my $payload (@payloads) {
        ++$n;
        ok( $broker->poll(%poptions), "Poll message $n ($proto) [$codec_name]" );
        ok( my $message = $broker->receive(), "Receive poll message $n ($proto) [$codec_name]" );
        ok( $broker->acknowledge($message), "Acknowledge poll message $n ($proto) [$codec_name]" );
        ok( $message->message->payload eq $payload, "Check paylod for message $n ($proto) [$codec_name]" );
    }
} ## end sub test_queue($;$$)

sub test_pingpong($;$$) {
    my ( $proto, $pproto, $codec ) = @_;

    $pproto ||= $proto;
    $codec  ||= $thrift;
    my $codec_name = ref($codec) ? $codec->name : $codec;

    ok(
        my $broker = SAPO::Broker::Clients::Simple->new(
            'codec'   => $codec,
            'host'    => $host,
            'proto'   => lc($pproto),
            'timeout' => 10,
        ),
        "Instantiate broker ($pproto)"
      );

	  my $aid = "${action_id}_$pproto";
	  ok($broker->ping($aid), "Send ping ($aid) [$codec_name]");
	  isa_ok(my $message = $broker->receive(), 'SAPO::Broker::Messages::Pong');
	  is($message->action_id(), $aid, 'Check action_id');
}

sub test_codec($) {
    my $codec = shift;
    test_queue( 'TCP', 'TCP', $codec );
    test_pingpong( 'TCP', 'TCP', $codec );
    test_queue( 'TCP', 'UDP', $codec );

    #ssl stuff
SKIP: {
        if ( not $ENV{'BROKER_DISABLE_SSL'} and SAPO::Broker::Utils::has_ssl() ) {
            test_queue( 'SSL', 'TCP', $codec );
            test_queue( 'SSL', 'UDP', $codec );
        } else {
            skip( $ENV{'BROKER_DISABLE_SSL'} ? "SSL tests disabled by env var" : "no SSL support", 6 + 16 * $N );
        }
    }
}

SKIP: {
    if ($protobuf) {
        test_codec('protobufxs');
    } else {
        skip( 'No protobuf support', 2 * ( 5 + 16 * $N ) + 1 );
    }
}

SKIP: {
    if ($thriftxs) {
        test_codec('thriftxs');
    } else {
        skip( 'No thriftxs support', 2 * ( 5 + 16 * $N ) + 1 );
    }
}

SKIP: {
    if ($thrift) {
        test_codec('thrift');
    } else {
        skip( 'No thrift support', 2 * ( 5 + 16 * $N ) + 1 );
    }
}

