package SAPO::Broker::Transport;

use SAPO::Broker::Transport::Message;
use SAPO::Broker::Utils qw(class);
use Carp qw(croak);

use strict;
use warnings;

class( 'mandatory' => [qw(codec)] );

sub send {
    my ( $self, $message ) = @_;

    my $transport_message = $self->codec->serialize($message);
    return $self->__write( $transport_message->serialize() );
}

sub receive {
    my ($self) = @_;

    #read the header
    my ( $type, $version, $length ) = SAPO::Broker::Transport::Message::__meta_from_header( $self->__read(8) );

    #read the payload
    my $payload = $self->__read($length);

    #now do the message parsing and "cast" into a common object for the messages

    return $self->codec->deserialize($payload);
}

1;
