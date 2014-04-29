class java {

  $packages = ['openjdk-7-jdk','maven']

  package { $packages:
    ensure => present,
  }

}

