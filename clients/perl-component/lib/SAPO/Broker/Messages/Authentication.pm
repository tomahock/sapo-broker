package SAPO::Broker::Messages::Authentication;

use SAPO::Broker::Utils qw(class);
use Readonly;

use strict;
use warnings;

Readonly::Array my @mandatory => qw(token authentication_type);
Readonly::Array my @optional  => qw(user_id role action_id);

class(
    'mandatory' => \@mandatory,
    'optional'  => \@optional,
);

1;
