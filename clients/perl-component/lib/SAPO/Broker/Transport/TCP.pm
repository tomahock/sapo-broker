package SAPO::Broker::Transport::TCP;

use Readonly;

use strict;
use warnings;

Readonly::Scalar my $DEFAULT_PORT => 3323;
Readonly::Scalar my $DEFAULT_HOST => 'localhost';

use base qw(SAPO::Broker::Transport::INET);

sub new {
    my $self = shift @_;

    return $self->SUPER::new(
        'host' => $DEFAULT_HOST,
        'port' => $DEFAULT_PORT,
        @_, 'proto' => 'tcp'
    );
}

1;
