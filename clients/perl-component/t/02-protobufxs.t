# Before `make install' is performed this script should be runnable with
# `make test'. After `make install' it should work as `perl SAPO-Broker.t'

#########################

# change 'tests => 1' to 'tests => last_test_to_print';

use Test::More tests => 2;
BEGIN { use_ok('SAPO::Broker::Utils') }

SKIP: {
    if (SAPO::Broker::Utils::has_protobufxs) {
        use_ok('SAPO::Broker::Codecs::Autogen::ProtobufXS::Atom');
    } else {
        skip( 'No support for protobuf(XS)', 1 );
    }
}

#########################

# Insert your test code below, the Test::More module is use()ed here so read
# its man page ( perldoc Test::More ) for help writing this test script.

