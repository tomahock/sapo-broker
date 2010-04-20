package SAPO::Broker::Messages::Authentication;

use SAPO::Broker::Utils qw(class);
use Readonly;

use strict;
use warnings;

Readonly::Array my @mandatory => qw(user_id role token);
Readonly::Array my @optional  => qw(authentication_type action_id);

class(
    'mandatory' => \@mandatory,
    'optional'  => \@optional,
);

1;
