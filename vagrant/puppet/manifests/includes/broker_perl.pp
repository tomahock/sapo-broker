class broker_perl {

  $packages = ['dh-make-perl', 'libxml2-dev', 'xml2', 'libprotobuf-dev', 'libprotoc-dev']

  package { $packages:
    ensure => present,
    before => Exec['download-protoxs']
  }

  $perl_modules = [ 'Thrift', 'Thrift::XS', 'IO::Socket::INET', 'Time::HiRes', 'Readonly', 'XML::LibXML', 'XML::LibXML::XPathContext', 'Test::More', 'AnyEvent', 'AnyEvent::Handle', 'Crypt::SSLeay', 'JSON::Any', 'LWP', 'IO::Socket::SSL']

  perl::module { $perl_modules:
  }

  #We need to get protobufxs module
  exec {'download-protoxs':
  	cwd => "/usr/local/src",
  	command => 'wget http://protobuf-perlxs.googlecode.com/files/protobuf-perlxs-1.1.tar.gz',
  	path => ['/usr/bin/'],
  	notify => Exec['unpack-protoxs']
  }

  exec { 'unpack-protoxs':
  	command => 'tar zxf protobuf-perlxs-1.1.tar.gz',
  	path    => ['/bin'],
  	cwd => "/usr/local/src",
  	notify => Exec['install-protoxs']
  }

  exec { 'install-protoxs':
  	cwd => "/usr/local/src/protobuf-perlxs-1.1",
    command => "/usr/local/src/protobuf-perlxs-1.1/configure && make && make install",
    logoutput   => true,
    refreshonly => true,
  }

}