package SAPO::Broker::Transport::UDP;

use base qw(SAPO::Broker::Transport::INET);

our $DEFAULT_PORT = 3323;
our $DEFAULT_HOST = 'localhost';

sub new {
    my $self = shift @_;

    return $self->SUPER::new(
        'host' => $DEFAULT_HOST,
        'port' => $DEFAULT_PORT,
        @_, 'proto' => 'udp'
    );
}

1;
