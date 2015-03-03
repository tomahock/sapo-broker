class perlbrew {

    $PERL_VERSION = '5.18.4' # Needs to be moved to hiera
    $PERL_NAME = "perl-${PERL_VERSION}"
    $PERLBREW_ROOT = "${USER_HOME}/perl5/perlbrew"
    $CPANM = "${PERLBREW_ROOT}/perls/${PERL_NAME}/bin/cpanm"
    $PERL = "${PERLBREW_ROOT}/perls/${PERL_NAME}/bin/perl"

    #$packages = ['dh-make-perl', 'libxml2-dev', 'xml2']

    #package { $packages:
    #    ensure => present,
    #    before => Exec['App::cpanminus Dependencies Installation']
    #}

    Exec {
        path => '/bin:/usr/bin',
        user => $USER,
        group => $USER,
        cwd => $USER_HOME,
        tries => 3,
        environment => ["PERLBREW_ROOT=${PERLBREW_ROOT}", "HOME=${USER_HOME}"]
    }

    File {
        owner => $USER,
        group => $USER,
        mode => 644
    }

    package { curl: ensure => latest }
    package { gcc: ensure => latest }
    package { dh-make-perl: ensure => latest }
    package { libxml2-dev: ensure => latest }
    package { xml2: ensure => latest }

    exec { 'Perlbrew Installation':
        require => Package['curl'],
        command => 'curl -kL http://install.perlbrew.pl | /bin/bash',
        creates => "${PERLBREW_ROOT}/bin/perlbrew"
    }

    exec { 'Perlbrew Initialization':
        require => Exec['Perlbrew Installation'],
        command => "${PERLBREW_ROOT}/bin/perlbrew init",
        creates => "${PERLBREW_ROOT}/etc/bashrc"
    }

    exec { 'Perlbrew Self Upgrade':
        require => Exec['Perlbrew Initialization'],
        command => "${PERLBREW_ROOT}/bin/perlbrew self-upgrade",
        tries => 5
    }

    define file_append($text) {
        exec { "echo '${text}' >> ${title}":
            require => Exec['Perlbrew Self Upgrade'],
            unless => "grep '${text}' ${title}",
            onlyif => "/usr/bin/test -w ${title}"
        }
    }

    file_append { "${USER_HOME}/.bashrc": text => "source ${PERLBREW_ROOT}/etc/bashrc" }

    ## Set `vagrant ssh' login to use perlbrew by default (turn off for debugging)
    file_append { "${USER_HOME}/.profile": text => "perlbrew switch ${PERL_VERSION}" }
    file_append { "${USER_HOME}/.bash_profile": text => "perlbrew switch ${PERL_VERSION}" }

    exec { 'Perl Installation':
        require => [Package['gcc'], Exec['Perlbrew Self Upgrade']],
        command => "${PERLBREW_ROOT}/bin/perlbrew install -j 4 ${PERL_VERSION}",
        creates => $PERL,
        timeout => 10000
    }

    exec { 'App::cpanminus Installation':
        require => [Package['curl'], Exec['Perl Installation']],
        provider => shell,
        command => "curl -L http://cpanmin.us | ${PERL} - --self-upgrade",
        creates => $CPANM
    }

    exec { 'App::cpanminus Self Upgrade':
        require => Exec['App::cpanminus Installation'],
        command => "${CPANM} --self-upgrade"
    }

    exec { 'App::cpanoutdated Installation':
        require => Exec['App::cpanminus Self Upgrade'],
        command => "${CPANM} App::cpanoutdated"
    }

    exec { 'App::cpanoutdated Execution':
        require => Exec['App::cpanoutdated Installation'],
        command => "${PERLBREW_ROOT}/perls/${PERL_NAME}/bin/cpan-outdated"
    }

    exec { 'Module::CPANfile Installation':
        require => Exec['App::cpanoutdated Execution'],
        command => "${CPANM} Module::CPANfile"
    }

    exec { 'App::cpanminus Dependencies Installation':
        require => [Package['dh-make-perl', 'libxml2-dev', 'xml2'], Exec['Module::CPANfile Installation']],
        provider => shell,
        command => "${CPANM} -q --installdeps /${USER}",
        onlyif => "/usr/bin/test -r /${USER}/cpanfile",
        logoutput => true
    }

}