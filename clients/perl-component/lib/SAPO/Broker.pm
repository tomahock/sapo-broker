package SAPO::Broker;

use strict;
use warnings;

our $VERSION = '0.1';

sub has_module($) {
    my ($module) = @_;

    eval "use $module;";

    return not $@;
}

sub has_ssl() {
    return has_module('IO::Socket::SSL');
}

sub has_sapo_sts() {
    return has_module('LWP') and has_module('Crypt::SSLeay') and has_module('JSON::Any');
}

1;
__END__

=head1 NAME

SAPO::Broker - Perl extension for using SAPO Broker

=head1 SYNOPSIS

  #see SAPO::Broker::Client::* and the eg directory for usage examples

=head1 DESCRIPTION

This is SAPO Broker's perl API.

=head1 AUTHOR

Cláudio Valente, E<lt>c.valente@co.sapo.ptE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2010 by SAPO

=cut
