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

 


#import box modules
import 'includes/*.pp'

class { 'java': }
class { 'misc': }
class { 'protobuf': }

include thrift

file { "/home/vagrant/.m2/settings.xml":
    owner => vagrant,
    group => vagrant,
    source => "puppet:///modules/maven-sapo/settings.xml"
}


