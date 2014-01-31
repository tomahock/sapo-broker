use utf8;
use strict;
use warnings;
use ExtUtils::MakeMaker;

my @PMLIBDIRS = ('lib');
my %PREREQ_PM = (
    'IO::Socket::INET'          => 0,
    'Time::HiRes'               => 0,
    'Readonly'                  => 0,
    'XML::LibXML'               => 0,
    'XML::LibXML::XPathContext' => 0,
    'Test::More'                => 0,
);

my @subdirs;

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

my $use_protobuf = 0;
if ( prompt( 'Do you want to install support for protobufs(XS)', 'yes' ) =~ /yes/i ) {
    push( @subdirs, 'protobufxs' );
    $use_protobuf = 1;
}

my $use_thrift = 1;
if ( prompt( 'Do you want to install support for Thrift', 'yes' ) !~ /yes/i ) {
    $use_thrift = 0;
}

my $use_thriftxs = 0;
if ( prompt( 'Do you want to install support for Thrift(XS)', 'yes' ) =~ /yes/i ) {
    $use_protobuf = 1;
}

if ( not( $use_thrift or $use_protobuf or $use_thriftxs ) ) {
    die "Thrift or protobuf codecs are mandatory. Please choose one.";
}

if ($use_thrift) {
    $PREREQ_PM{'Thrift'} = '0.1';
}

if ($use_thriftxs) {
    $PREREQ_PM{'Thrift::XS'} = '0.1';
}

WriteMakefile(
    'DIR'           => \@subdirs,
    'NAME'          => 'SAPO::Broker',
    'AUTHOR'        => 'Cláudio Valente <c.valente@co.sapo.pt>',
    'VERSION_FROM'  => 'lib/SAPO/Broker/Clients.pm',
    'ABSTRACT_FROM' => 'lib/SAPO/Broker/Clients.pm',
    'PMLIBDIRS'     => [@PMLIBDIRS],
    'PREREQ_PM'     => {%PREREQ_PM},
    'test'          => { 'TESTS' => 't/*.t' },
    dist            => {
        COMPRESS => 'gzip -9f',
        SUFFIX   => 'gz',
    },

);
