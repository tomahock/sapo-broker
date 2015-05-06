SAPO-Broker version 0.01
========================

LATEST INSTRUCTIONS:

To build the client with the vagrant provided, just type:

make clean
perl Makefile.PL
make && make test

If all went right and all tests pass, you can install the module:

sudo make install

## Installing from source

```bash
git clone git@github.com:sapo/sapo-broker.git
cd sapo-broker/clients/perl && perl Makefile.PL
make install
```

During build, must select at least one of the `thrift` or `protobuf` codecs, otherwise the `makefile` won't be created

## Dependencies

### cpan

```bash
cpan install Readonly

```


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
Or install from debian/ubuntu repositories:

```bash
sudo apt-get install libprotobuf-dev
```


## Testing

The build process also runs the tests. By default tests connect to the broker in localhost. You can change this for a broker server running in another host by setting the environment variable `BROKER_HOST`.

> If the test broker doesn't have SSL support you should define `BROKER_DISABLE_SSL` to 1.
