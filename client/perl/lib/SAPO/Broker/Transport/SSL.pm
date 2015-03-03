package SAPO::Broker::Transport::SSL;

use IO::Socket::SSL;
use Readonly;
use Data::Dumper;

use strict;
use warnings;

use base qw(SAPO::Broker::Transport::TCP);

Readonly::Scalar my $DEFAULT_PORT => 3490;
Readonly::Scalar my $DEFAULT_HOST => 'localhost';

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
    $original_socket->blocking(1);
    #warn 'options';
    #warn Dumper(\@_);

    my @params = @_;
    #push @params, 'key', 'value'


    $self->{'__socket'} = IO::Socket::SSL->start_SSL( $original_socket, @params ); #Commented because IO::Socket::SSL->start_SSL does not return nothing
    #IO::Socket::SSL->start_SSL( $original_socket, @_ );
    $self->{'__original_socket'} = $original_socket;

    return $self;
} ## end sub new

#redefine read/write
#timeouts not supported

sub __write {
    my ( $self, $payload ) = @_;

    my $i = 1;
    warn "Stack Trace:\n";
    while ( (my @call_details = (caller($i++))) ){
        warn $call_details[1].":".$call_details[2]." in function ".$call_details[3]."\n";
    }

    warn "__write self: " . Dumper($self);
    my $sock        = $self->{'__socket'};
    warn Dumper($sock);
    my $tot_written = 0;
    my $tot_write   = length($payload);

    while ( $tot_written < $tot_write ) {
        local $SIG{'PIPE'} = 'IGNORE';
        my $written = $sock->syswrite( $payload, $tot_write - $tot_written, $tot_written );

        if ( not defined $written ) {

            #ERROR
            #TODO: What about EINTR?
            die "Error writing socket. $!";
        } elsif ( 0 == $written ) {

            #EOF
            die "Unexpected EOF while writing";
        } else {

            #OK
            $tot_written += $written;
        }
    }
    return $self;
} ## end sub __write

sub __read {
    my ( $self, $len ) = @_;

    my $sock = $self->{'__socket'};
    my $payload;
    my $tot_read = 0;

    while ( $tot_read < $len ) {
        local $SIG{'PIPE'} = 'IGNORE';
        my $read = $sock->sysread( $payload, $len - $tot_read, $tot_read );

        if ( not defined $read ) {

            #ERROR
            #TODO: What about EINTR?
            die "Error reading from socket. $!";
        } elsif ( 0 == $read ) {

            #EOF
            die "Unexpected EOF while reading";
        } else {

            #OK
            $tot_read += $read;
        }
    }
    return $payload;
} ## end sub __read

sub DESTROY {
    my ($self) = @_;

    my $socket = $self->{'__socket'};

    if ($socket) {
        return $socket->close(
            'SSL_ctx_free'      => 1,
            'SSL_fast_shutdown' => 0
        );
    } else {
        return;
    }
}

1;
