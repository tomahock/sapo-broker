class misc {

  $packages = ['vim','xmlto', 'autogen']

  package { $packages:
    ensure => present,
  }

  exec{ 'retrieve_sapo_cert':
  	command => 'wget https://id.sapo.pt/ca/sapo.crt',
  	cwd => '/usr/local/share/ca-certificates/',
  	path => ['/usr/bin/'],
  	user => root,
    group => staff,
    tries => 3
  }

}