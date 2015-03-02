class protobuf {

  $packages = ['protobuf-compiler']

  package { $packages:
    ensure => present,
  }

  #We need to get protobufxs module
  exec {'download-protoxs':
  	cwd => "/usr/local/src",
  	command => 'wget http://protobuf-perlxs.googlecode.com/files/protobuf-perlxs-1.1.tar.gz',
  	path => ['/usr/bin/']
  }

  exec { 'unpack-protoxs':
    require => Exec['download-protoxs'],
  	command => 'tar zxf protobuf-perlxs-1.1.tar.gz',
  	path    => ['/bin'],
  	cwd => "/usr/local/src"
  }

  exec { 'install-protoxs':
    require => Exec['unpack-protoxs'],
  	cwd => "/usr/local/src/protobuf-perlxs-1.1",
    command => "/usr/local/src/protobuf-perlxs-1.1/configure && make && make install",
    logoutput   => true,
    refreshonly => true,
  }

}

