class misc {

  $packages = ['vim','xmlto']

  package { $packages:
    ensure => present,
  }

}

