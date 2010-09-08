use utf8;
use strict;
use warnings;
use ExtUtils::MakeMaker;

my %PREREQ_PM = ( 'SAPO::Broker' => 0, );

WriteMakefile(
    'NAME' => 'SAPO::Broker::Codecs::Autogen::ProtobufXS::Atom',
    'dist' => {
        'COMPRESS' => 'gzip -9f',
        'SUFFIX'   => 'gz',
    },
    'AUTHOR'        => 'Cláudio Valente <c.valente@co.sapo.pt>',
    'VERSION_FROM'  => 'Atom.pm',
    'ABSTRACT_FROM' => 'Atom.pm',
    'PREREQ_PM'     => {%PREREQ_PM},

    'OPTIMIZE' => '-O2 -Wall',
    'CC'       => 'g++',
    'LD'       => '$(CC)',
    'CCFLAGS'  => '-fno-strict-aliasing -Wall',
    'OBJECT'   => 'broker.pb.o Atom.o',
    'LIBS'     => [" -L/usr/local/lib -lprotobuf"],
    'INC'      => " -I/usr/local/include",
    'XSOPT'    => '-C++',
);
