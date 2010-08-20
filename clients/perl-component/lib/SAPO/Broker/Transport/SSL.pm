package SAPO::Broker::Transport::SSL;

use IO::Socket::SSL;

use strict;
use warnings;

our $DEFAULT_PORT = 3390;
our $DEFAULT_HOST = 'localhost';

use base qw(SAPO::Broker::Transport::TCP);

sub new {
    my $self = shift @_;

    #initialize
    $self = $self->SUPER::new(
        'host' => $DEFAULT_HOST,
        'port' => $DEFAULT_PORT,
        @_, 'proto' => 'tcp'
    );

    #now do the SSL handshake on the tcp socket

    my $original_socket = $self->{'__socket'};

    #and use the new wrapped socket as if it were a regular TCP socket
    #XXX this can be problematic for the select loop
    #in case of select related bugs just reimplement __read and __write without timeouts

    $self->{'__socket'} = IO::Socket::SSL->start_SSL( $original_socket, @_ );
    $self->{'__original_socket'} = $original_socket;

    return $self;
} ## end sub new

1;
