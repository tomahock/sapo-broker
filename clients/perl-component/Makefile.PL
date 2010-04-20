use 5.006;
use ExtUtils::MakeMaker;
use utf8;

# See lib/ExtUtils/MakeMaker.pm for details of how to influence
# the contents of the Makefile that is written.
WriteMakefile(
    NAME              => 'SAPO::Broker',
    VERSION_FROM      => 'lib/SAPO/Broker.pm', # finds $VERSION
    PREREQ_PM         => {}, # e.g., Module::Name => 1.1
    ($] >= 5.005 ?     ## Add these new keywords supported since 5.005
      (ABSTRACT_FROM  => 'lib/SAPO/Broker.pm', # retrieve abstract from module
       AUTHOR         => 'Cláudio Valente <c.valente@co.sapo.pt>') : ()),
);
