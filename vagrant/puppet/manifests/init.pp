
class apt {
  exec { "apt-update":
    command => "/usr/bin/apt-get update"
  }

  # Ensure apt-get update has been run before installing any packages
  Exec["apt-update"] -> Package <| |>
}

class { 'apt': }

#import box modules
import 'includes/*.pp'

class { 'java': }

include thrift

file { "/home/vagrant/.m2/settings.xml":
    owner => vagrant,
    group => vagrant,
    source => "puppet:///modules/maven-sapo/settings.xml"
}
