use Test::More tests => 1;

use strict;
use warnings;

ok( unlink('.broker_name'),   'Removed broker_name file' );
