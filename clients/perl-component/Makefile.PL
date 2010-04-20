use strict;
use warnings;
use ExtUtils::MakeMaker;

WriteMakefile(
    NAME                => 'SAPO::Broker',
    AUTHOR              => 'Cláudio Valente <c.valente@co.sapo.pt>',
    VERSION_FROM        => 'lib/SAPO/Broker.pm',
    ABSTRACT_FROM       => 'lib/SAPO/Broker.pm',
    PL_FILES            => {},
    PREREQ_PM => {
    'Thrift' => 0,
    },
);
