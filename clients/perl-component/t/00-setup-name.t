use Time::HiRes qw(time);
use Net::Domain qw(hostfqdn);

use Test::More tests => 2;

use strict;
use warnings;

my $rand_name = '/perl/tests/' . hostfqdn() . "_" . time();

sub write_name {
    eval {
        open my $f, '>', '.broker_name' or die $!;
        print $f $rand_name;
        close($f) or die !$;;
    };
    if ($@) {
        warn $@;
        return 0;
    } else {
        return 1;
    }

}

ok( $rand_name,   'Generate topic name' );
ok( write_name(), 'Save name in file' );
