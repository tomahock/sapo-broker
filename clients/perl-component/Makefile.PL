use utf8;
use strict;
use warnings;
use ExtUtils::MakeMaker;

my @PMLIBDIRS = ('lib');
my %PREREQ_PM = (
    'Thrift'           => '0.1',
    'IO::Socket::INET' => 0,
    'Time::HiRes'      => 0,
    'Readonly'         => 0,
);

sub has_module($) {
    my ($module) = @_;

    eval "use $module;";

    return not $@;
}

unless ( has_module 'LWP' and has_module 'Crypt::SSLeay' and has_module 'JSON::Any' ) {
    if ( prompt( 'Do you want to use the HTTP interface to authenticate with SAPO STS?', 'yes' ) =~ /^yes/i ) {
        $PREREQ_PM{'Crypt::SSLeay'} = 0;
        $PREREQ_PM{'LWP'}           = 0;
        $PREREQ_PM{'JSON::Any'}     = 0;
    }
}

unless ( has_module "IO::Socket::SSL" ) {
    if ( prompt( 'Do you want to use the SSL transport', 'yes' ) =~ /^yes/i ) {
        $PREREQ_PM{'IO::Socket:SSL'} = 0;
    }
}

WriteMakefile(
    'NAME'          => 'SAPO::Broker',
    'AUTHOR'        => 'Cláudio Valente <c.valente@co.sapo.pt>',
    'VERSION_FROM'  => 'lib/SAPO/Broker.pm',
    'ABSTRACT_FROM' => 'lib/SAPO/Broker.pm',
    'PMLIBDIRS'     => [@PMLIBDIRS],
    'PREREQ_PM'     => {%PREREQ_PM},
    'test'          => { 'TESTS' => 't/*.t' },
);
