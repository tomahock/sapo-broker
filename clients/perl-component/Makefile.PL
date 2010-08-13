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

=cut
eval {

    #try to find Thrift
    require Thrift;
};
if ($@) {

    #Thrift not installed so to make this easier we ship and install a Thrift version
    push @PMLIBDIRS, 'thrift_lib';
}else{
    #Thrift found, add as dependency
    $PREREQ_PM{'Thrift'} = '0.1';
}
=cut

WriteMakefile(
    NAME          => 'SAPO::Broker',
    AUTHOR        => 'Cláudio Valente <c.valente@co.sapo.pt>',
    VERSION_FROM  => 'lib/SAPO/Broker.pm',
    ABSTRACT_FROM => 'lib/SAPO/Broker.pm',
    PMLIBDIRS     => [@PMLIBDIRS],
    PREREQ_PM     => {%PREREQ_PM},
);
