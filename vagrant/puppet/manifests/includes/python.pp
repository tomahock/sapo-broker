class python {

  $packages = ['python-dev','python-pip']

  package { $packages:
    ensure => present,
  }

}

