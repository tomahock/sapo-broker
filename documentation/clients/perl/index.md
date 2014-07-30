---
layout: broker-documentation
title: Perl
---

# Using the Perl client

## Installing from source

```bash
git clone git@github.com:sapo/sapo-broker.git
cd sapo-broker/clients/perl-component && perl Makefile.PL
make install
```

During build, must select at least one of the `thrift` or `protobuf` codecs, otherwise the `makefile` won't be created

## Dependencies

### Thrift

The build process should be similar to:

```bash
wget 'http://www.apache.org/dist//incubator/thrift/XXX-incubating/thrift-XXX.tar.gz'
tar -xzf thrift-XXX.tar.gz 
cd thrift-XXX/lib/perl/
perl Makefile.PL
#you may need to install dependencies from CPAN
make
sudo make install
```

### Protobuf

Most distributions will have `protobuf` packages, but you can always compile and install from source as follows:

```bash
wget http://protobuf.googlecode.com/files/protobuf-XXX.tar.bz2
tar -xjf protobuf-XXX.tar.bz2
cd protobuf-XXX
./configure
make
sudo make install
```

## Testing

The build process also runs the tests. By default tests connect to the broker in localhost. You can change this for a broker server running in another host by setting the environment variable `BROKER_HOST`.

> If the test broker doesn't have SSL support you should define `BROKER_DISABLE_SSL` to 1.

## Perl Client Usage

Here's a simple producer/publisher:

```perl
use SAPO::Broker::Clients::Simple;
        
use strict;
use warnings;
        
# connects to localhost using tcp by default (can also use udp or ssl)
my $broker = SAPO::Broker::Clients::Simple->new(host=>'localhost', proto=>'tcp'); 

my %options = (
    'destination_type' => 'QUEUE', #can also be TOPIC
    'destination' => '/tests/perl',
);

# now publish something
$broker->publish(%options, 'payload' => "This is the payload");

# and subscribe to something
$broker->subscribe(%options, auto_acknowledge => 1); # auto_acknowledge makes life simpler
my $notification = $broker->receive;
my $payload = $notification->message->payload;
```