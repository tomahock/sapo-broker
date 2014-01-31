use Test::More tests => 2;

use strict;
use warnings;

ok( unlink('.broker_info'), 'Removed broker info file' );
ok( unlink('.broker_data'), 'Removed broker data file' );
