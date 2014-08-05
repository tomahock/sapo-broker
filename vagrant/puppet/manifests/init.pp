class forceupdate {
  exec { "apt-update":
    command => "/usr/bin/apt-get update"
  }

  # Ensure apt-get update has been run before installing any packages
  Exec["apt-update"] -> Package <| |>
}

class { 'forceupdate': }


class { 'apt':
  always_apt_update    => true
}

include apt

apt::key { 'sapodebiankey':
  key        => '29607E3913BD8AC6',
  key_source => 'http://mirrors.bk.sapo.pt/debian/sapo/gpg-key-sapo-packages',
}

apt::source { 'debianmirror':
  location   => 'http://mirrors.bk.sapo.pt/debian/sapo',
  release    => 'wheezy',
  repos      => 'sapo'
}

class { 'ohmyzsh': }

# for a single user
ohmyzsh::install { 'vagrant': }

# activate plugins for a user
ohmyzsh::plugins { 'vagrant': plugins => 'github z' }


#import box modules
import 'includes/*.pp'

class { 'java': }
class { 'misc': }
class { 'protobuf': }
class { 'python': }
class { 'php': }

include thrift

file { "/home/vagrant/.m2/":

    ensure    => "directory",
    owner     => "vagrant",
    group => "vagrant"

}->file { "/home/vagrant/.m2/settings.xml":
    owner => vagrant,
    group => vagrant,
    source => "puppet:///modules/sapo-maven/settings.xml"
}

Class['python']->Class['thrift']

