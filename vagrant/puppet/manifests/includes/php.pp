class php {

  $packages = ['php5-dev','libsapo-broker2-dev','dh-make-php','debhelper', 'build-essential']

  package { $packages:
    ensure => present,
  }

}

