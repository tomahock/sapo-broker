class protobuf {

  $packages = ['protobuf-compiler']

  package { $packages:
    ensure => present,
  }

}

