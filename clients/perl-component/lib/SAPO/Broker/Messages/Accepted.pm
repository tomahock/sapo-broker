package SAPO::Broker::Messages::Accepted;

use SAPO::Broker::Utils qw(class);
use Readonly;

use strict;
use warnings;

Readonly::Array my @mandatory => qw();
Readonly::Array my @optional  => qw(action_id);

class(
    'mandatory' => \@mandatory,
    'optional'  => \@optional,
);

1;
