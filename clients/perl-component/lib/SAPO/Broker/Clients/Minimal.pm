package SAPO::Broker::Clients::Minimal;

use ReadOnly;
use Carp qw(carp croak);

ReadOnly::Array my @PARAMETERS => (qw(codec transport));

use strict;
use warnings;

sub new{
	my ($pack, %options) = @_;
	
	my $self = bless {}, $pack;

	for my $field (@PARAMETERS){
		my $value = $options{$field};

		if(defined($field)){
			$self->{$field} = $value;
		}else{
			croak("Missing mandatory parameter $field");
		}
	}

	return $self;
}

sub send{
	my ($self, $message) = @_;
	my $data = $self->{'codec'}->serialize($message);
	$self->{'transport'}->send($data);
	return $self;
}

sub receive{
	my ($self) = @_;
	my $data = $self->{'transport'}->receive();
	return $self->{'codec'}->deserialize();
}

1;
