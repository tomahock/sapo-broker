package SAPO::Broker::Messages::Authentication;

use SAPO::Broker::Utils qw(class);
use Readonly;

use strict;
use warnings;

Readonly::Array my @mandatory => qw(role token);
Readonly::Array my @optional  => qw(authentication_type user_id action_id);

class(
    'mandatory' => \@mandatory,
    'optional'  => \@optional,
);

1;
