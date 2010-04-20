package SAPO::Broker::Transport::INET;

use IO::Socket::INET ();
use Socket qw(MSG_NOSIGNAL);
use Time::HiRes qw(time);
use IO::Select ();
use Carp qw(croak);

use strict;
use warnings;

use base qw(SAPO::Broker::Transport);

my %parameters_map = (
    'host'  => 'PeerAddr',
    'port'  => 'PeerPort',
    'proto' => 'Proto',
);

sub new {
    my $class = shift @_;
    my %params = @_;

    my $self = $class->SUPER::new(%params);

    my %sock_params = (
        Reuse    => 1,
        Blocking => 1,
    );

    while ( my ( $source, $destination ) = each(%parameters_map) ) {
        my $value = $params{$source};
        if ( defined $value ) {
            $sock_params{$destination} = $value;
        } else {
            croak("Missing mandatory parameter $source");
        }
    }

    #now do our own thing;
    my $socket = IO::Socket::INET->new(%sock_params);

    if ( defined $socket ) {
        $socket->autoflush(0);
        my $select = IO::Select->new($socket);
        return bless {
            %params,
            '__socket' => $socket,
            '__select' => $select,
        } => $class;
    } else {
        die "Couldn't create socket. $!\n";
    }
} ## end sub new

sub __write {
    my ( $self, $payload, $timeout ) = @_;

    use bytes;

    my $select      = $self->{'__select'};
    my $tot_writen  = 0;
    my $tot_write   = length($payload);

    while ( ( not defined($timeout) or $timeout > 0 ) and $tot_writen < $tot_write ) {
        my $start = time();
        my ($sock) = $select->can_write($timeout);
        if ($sock) {
            my $writen;
            {
            local $SIG{'PIPE'} = 'IGNORE';
            $writen = $sock->syswrite( $payload, $tot_write - $tot_writen, $tot_writen );
            }
            my $delta = time() - $start;
            if ( not defined $writen ) {

                #ERROR
                #TODO: What about EINTR?
                die "Error writing socket. $!";
            } elsif ( 0 == $writen ) {

                #EOF
                die "Unexpected EOF while writing";
            } else {

                #OK
                $tot_writen += $writen;
                if ( defined $timeout ) {
                    $timeout -= $delta;
                }
            }
        } else {

            #timeout, raise exception
            die "Write timeout. $!";
        }
    } ## end while ( ( not defined($timeout...
    if ( $tot_writen == $tot_write ) {
        return $tot_writen;
    } else {
        die "Error writing to socket";
    }
} ## end sub __write

sub __read {
    my ( $self, $len, $timeout ) = @_;

    use bytes;

    my $select = $self->{'__select'};
    my $buf    = '';

    while ( ( not defined($timeout) or $timeout > 0 ) and $len > 0 ) {
        my $start = time();
        my ($sock) = $select->can_read($timeout);
        if ($sock) {
            my $read = $sock->sysread( $buf, $len, length($buf) );
            my $delta = time() - $start;
            if ( not defined $read ) {

                #ERROR
                #TODO: What about EINTR?
                die "Error reading socket. $!";
            } elsif ( 0 == $read ) {

                #EOF
                die "Unexpected EOF while reading";
            } else {

                #OK
                $len -= $read;
                if ( defined $timeout ) {
                    $timeout -= $delta;
                }
            }
        } else {

            #timeout, raise exception
            die "Read timeout. $!";
        }
    } ## end while ( ( not defined($timeout...
    if ( 0 == $len ) {
        return $buf;
    } else {
        die "Error reading from socket";
    }
} ## end sub __read

package SAPO::Broker::Transport::TCP;

our $DEFAULT_PORT = 3323;
our $DEFAULT_HOST = 'localhost';

use base qw(SAPO::Broker::Transport::INET);

sub new {
    my $self = shift @_;

    return $self->SUPER::new(
        'host' => $DEFAULT_HOST,
        'port' => $DEFAULT_PORT,
        @_, 'proto' => 'tcp'
    );
}

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
