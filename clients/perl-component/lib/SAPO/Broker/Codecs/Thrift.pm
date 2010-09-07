package SAPO::Broker::Codecs::Thrift;

use SAPO::Broker::Codecs::Autogen::Thrift::Types;
use SAPO::Broker::Messages;
use SAPO::Broker::Transport;
use Thrift::BinaryProtocol;
use Thrift::MemoryBuffer;
use Readonly;
use Carp qw(carp croak);

use strict;
use warnings;

Readonly::Scalar my $ENCODING_TYPE => 2;    #Thrift encoding type

Readonly::Hash my %_string2kind => (
    'TOPIC'         => SAPO::Broker::Codecs::Autogen::Thrift::DestinationType::TOPIC,
    'QUEUE'         => SAPO::Broker::Codecs::Autogen::Thrift::DestinationType::QUEUE,
    'VIRTUAL_QUEUE' => SAPO::Broker::Codecs::Autogen::Thrift::DestinationType::VIRTUAL_QUEUE,
);

Readonly::Hash my %_kind2string => reverse(%_string2kind);

sub new {
    my ($class) = @_;
    return bless {}, $class;
}

sub name {
    return 'Thrift';
}

sub _string2kind($) {
    my ($kind) = @_;

    my $res = $_string2kind{$kind};
    if ( defined $res ) {
        return $res;
    } else {
        croak("Unknown kind $kind");
    }
}

sub _kind2string($) {
    my ($kind) = @_;

    my $res = $_kind2string{$kind};
    if ( defined $res ) {
        return $res;
    } else {
        croak("Unknown kind $kind");
        return;
    }
}

sub serialize_publish($$) {
    my ( $message, $action ) = @_;

    my $broker_message = SAPO::Broker::Codecs::Autogen::Thrift::BrokerMessage->new( $message->message() );
    my $publish        = SAPO::Broker::Codecs::Autogen::Thrift::Publish->new($message);

    #XXX ugly kludge to workaround a broker bug that doesn't check whether the field is sent over the wire
    for my $field (qw(timestamp expiration)) {
        if ( not defined $broker_message->$field() ) {
            $broker_message->$field(-1);
        }
    }

    $publish->message($broker_message);
    $publish->destination_type( _string2kind( $message->destination_type() ) );

    $action->action_type(SAPO::Broker::Codecs::Autogen::Thrift::ActionType::PUBLISH);
    $action->publish($publish);
    return $action;
} ## end sub serialize_publish($$)

sub serialize_poll($$) {
    my ( $message, $action ) = @_;

    my $poll = SAPO::Broker::Codecs::Autogen::Thrift::Poll->new($message);

    $action->action_type(SAPO::Broker::Codecs::Autogen::Thrift::ActionType::POLL);
    $action->poll($poll);
    return $action;
}

sub parse_accepted($) {
    my ($action) = @_;
    return SAPO::Broker::Messages::Accepted( $action->accepted() );
}

sub serialize_acknowledge($$) {
    my ( $message, $action ) = @_;

    my $acknowledge = SAPO::Broker::Codecs::Autogen::Thrift::Acknowledge->new($message);

    $action->action_type(SAPO::Broker::Codecs::Autogen::Thrift::ActionType::ACKNOWLEDGE);
    $action->ack_message($acknowledge);
    return $action;
}

sub serialize_subscribe($$) {
    my ( $message, $action ) = @_;

    my $subscribe = SAPO::Broker::Codecs::Autogen::Thrift::Subscribe->new($message);

    #must set correct kind
    $subscribe->destination_type( _string2kind( $message->destination_type() ) );

    $action->action_type(SAPO::Broker::Codecs::Autogen::Thrift::ActionType::SUBSCRIBE);
    $action->subscribe($subscribe);
    return $action;
}

sub serialize_unsubscribe($$) {
    my ( $message, $action ) = @_;

    my $unsubscribe = SAPO::Broker::Codecs::Autogen::Thrift::Unsubscribe->new($message);

    #must set correct kind
    $unsubscribe->destination_type( _string2kind( $message->destination_type() ) );

    $action->action_type(SAPO::Broker::Codecs::Autogen::Thrift::ActionType::UNSUBSCRIBE);
    $action->unsubscribe($unsubscribe);
    return $action;
}

sub parse_notification($) {
    my ($action)     = @_;
    my $notification = SAPO::Broker::Messages::Notification->new( $action->notification() );
    my $message      = $notification->{'message'};
    $message->{'id'}           = $message->{'message_id'};
    $notification->{'message'} = SAPO::Broker::Messages::Message->new($message);

    #now cast the actual message containing the payload
    $notification->message( SAPO::Broker::Messages::Message->new($message) );
    return $notification;
}

sub parse_fault($) {
    my ($action) = @_;

    return SAPO::Broker::Messages::Fault->new( $action->fault() );
}

sub serialize_ping($$) {
    my ( $message, $action ) = @_;

    my $ping = SAPO::Broker::Codecs::Autogen::Thrift::Ping->new($message);

    $action->action_type(SAPO::Broker::Codecs::Autogen::Thrift::ActionType::PING);
    $action->ping($ping);
    return $action;
}

sub parse_pong($) {
    my ($action) = @_;

    return SAPO::Broker::Messages::Pong->new( $action->pong() );
}

sub serialize_authentication($$) {
    my ( $message, $action ) = @_;

    my $auth = SAPO::Broker::Codecs::Autogen::Thrift::Authentication->new($message);

    $action->action_type(SAPO::Broker::Codecs::Autogen::Thrift::ActionType::AUTH);
    $action->auth($auth);
    return $action;
}

my %__dispatch_serialize = (
    'SAPO::Broker::Messages::Publish'        => \&serialize_publish,
    'SAPO::Broker::Messages::Poll'           => \&serialize_poll,
    'SAPO::Broker::Messages::Acknowledge'    => \&serialize_acknowledge,
    'SAPO::Broker::Messages::Subscribe'      => \&serialize_subscribe,
    'SAPO::Broker::Messages::Unsubscribe'    => \&serialize_unsubscribe,
    'SAPO::Broker::Messages::Ping'           => \&serialize_ping,
    'SAPO::Broker::Messages::Authentication' => \&serialize_authentication,
);

my %__dispatch_deserialize = (
    SAPO::Broker::Codecs::Autogen::Thrift::ActionType::NOTIFICATION => \&parse_notification,
    SAPO::Broker::Codecs::Autogen::Thrift::ActionType::FAULT        => \&parse_fault,
    SAPO::Broker::Codecs::Autogen::Thrift::ActionType::PONG         => \&parse_pong,
    SAPO::Broker::Codecs::Autogen::Thrift::ActionType::ACCEPTED     => \&parse_accepted,
);

sub serialize {
    my ( $self, $message ) = @_;

    #try to find the serializer
    #could use ref but I wan't to make this robust to serialize subclasses of broker messages
    #since there are few classes, iterating should not be much slower that using the hash table

    while ( my ( $class, $serializer ) = each(%__dispatch_serialize) ) {
        if ( $message->isa($class) ) {

            #reset the hash iterator
            scalar keys(%__dispatch_serialize);

            my $action = SAPO::Broker::Codecs::Autogen::Thrift::Action->new();

            #populate action
            $serializer->( $message, $action );

            #create the data structure's outer shell
            my $atom = SAPO::Broker::Codecs::Autogen::Thrift::Atom->new( { action => $action } );

            #serialize the atom into bytes
            my $transport = Thrift::MemoryBuffer->new();
            my $protocol  = Thrift::BinaryProtocol->new($transport);
            $atom->write($protocol);
            my $payload = $transport->getBuffer();

            return SAPO::Broker::Transport::Message->new(
                type    => $ENCODING_TYPE,
                version => 1,
                payload => $payload
            );
        } ## end if ( $message->isa($class...
    } ## end while ( my ( $class, $serializer...

    croak("Can't serialize $message");
    return;
} ## end sub serialize

sub __deserialize {
    my ( $self, $payload ) = @_;

    my $atom = SAPO::Broker::Codecs::Autogen::Thrift::Atom->new();

    my $transport = Thrift::MemoryBuffer->new();
    $transport->write($payload);
    my $protocol = Thrift::BinaryProtocol->new($transport);
    $atom->read($protocol);

    return $atom;
}

sub deserialize {
    my ( $self, $payload ) = @_;

    my $atom   = $self->__deserialize($payload);
    my $action = $atom->action();

    my $deserialize = $__dispatch_deserialize{ $action->action_type() };
    if ($deserialize) {
        return $deserialize->($action);
    } else {
        croak( "Unknown action_type " . $action->action_type() . ". Can't deserialize" );
        return;
    }
}

1;
