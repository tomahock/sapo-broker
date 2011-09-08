# Before `make install' is performed this script should be runnable with
# `make test'. After `make install' it should work as `perl SAPO-Broker.t'

#########################

# change 'tests => 1' to 'tests => last_test_to_print';

use Test::More tests => 6;
BEGIN { use_ok('SAPO::Broker') }
BEGIN { use_ok('SAPO::Broker::Clients::Minimal') }
BEGIN { use_ok('SAPO::Broker::Clients::Simple') }
BEGIN { use_ok('SAPO::Broker::Transport::TCP') }
BEGIN { use_ok('SAPO::Broker::Transport::UDP') }
BEGIN { use_ok('SAPO::Broker::Messages') }

#########################

# Insert your test code below, the Test::More module is use()ed here so read
# its man page ( perldoc Test::More ) for help writing this test script.

