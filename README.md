sapo-broker
===========

SAPO Broker is a high performance distributed messaging framework. Among other features, it provides minimal administration overhead, Publish-Subscribe and Point-to-Point messaging, guaranteed delivery and wildcard subscriptions.

SAPO Broker is written in Java, but has client libraries for [Perl][pl], [Python][py], [PHP][php], [.NET][net], [C][c] and quite a few [others][o], plus you can talk to it using Thrift, XML or JSON.

To start using SAPO Broker take a look at the Quick Start tutorial, and then please read the User Guide for more in depth information. Both are provided as part of the source tree in DocBook format.

You can access our public broker server at <code>broker.labs.sapo.pt</code> and subscribe to a number of public topics to experiment with.

## How do I get started?

Download the latest distribution bundle and read the Quick Start. To learn more about the design and concepts of SAPO Broker read the User Guide.

You can fetch the latest version of the SAPO Broker source code from our Git repo. For checking out the code use the <code>git</code> command line client as follows:

<pre class="prettyprint">
git clone git@github.com:sapo/sapo-broker.git
</pre>

## License

The SAPO Broker core is distributed under the [BSD license][bsd]. Some dependencies or extensions are under [separate licensing][l].

## How to get involved

Go ahead and [fork][repo] the project or subscribe to the [mailing-list][ml].

----

# Using the PHP Client

The `SAPO_Broker` class abstracts all the low-level complexity of the SAPO Broker and gives the developer a simple to use high-level API to build event consumers and producers.

It will work with at least PHP 4.3.0 but PHP >5.2.x is recomended. Minimum PHP requirements are the `mbstring` extension and streams support for socket communications. Most standard PHP distributions have these.

This class is event driven and provides the  main loop for your program. Running an event consumer with this class requires only 3 simple steps:

* Initializing the class `$broker=new SAPO_Broker;`
* Subscribing to the desired topics `$broker->subscribe(...);`
* Running the main loop `$broker->consumer();`

A very simple (but complete) consumer can look like this:

```php
include('classes/broker.php');
$broker=new SAPO_Broker;
$broker->subscribe('/sapo/homepage/visits',NULL,"processEvent");
$broker->consumer();

function processEvent($payload) {
  echo "Someone has visited the Homepage\n";
  echo $payload."\n";
}
```

Lets look into each step in detail now:

## Class initialization

The first thing to do is initializing the class. You can do this at any stage in your code. It as simple as:

```php
$broker=new SAPO_Broker;
```

When initializing the `SAPO_Broker class`, you can provide a series of parameters to tune your preferences. All of them are optional. These are:

### server

Defines the IP address of the broker agent to which connect.

If this parameter is not supplied then a series of tests are conducted to provide auto-discovery for the nearest agent:

* The environment variable `SAPO_BROKER_SERVER` is checked. If it exists, then it will be used.
* Tries to connect to localhost (127.0.0.1). Will be used if successful.
* Check for the existence of an `/etc/world_map.xml` file. Picks a random agent from it. Tries to connect. Uses it if successful (otherwise repeats 2 more times).
* Uses the last resort DNS name `broker.bk.sapo.pt`, which is a round robin record to a few agents.

### port

TCP port in which the Broker is listening for clients. Default is 3332 (for this transport).

### debug

Setting `debug` to true will output a lot of useful information for the developer. Default is false.

### timeout

Maximum time (in seconds) for inactivity (sent or received data) after which the connection drops and reconnects. Default is 5 minutes.

### locale

Locale setting used in multibyte functions. Default is `pt_PT`.

### force_expat

Force the XML parser to use `expat`. Default is false.

### force_dom

Force the XML parser to use native PHP DOM support. Default is false.

Parameters are passed inside an array structure like this:


```php
$opts=array('debug'=>TRUE
            'server'=>'10.135.0.1'
            'force_dom'=>TRUE);

$broker=new SAPO_Broker($opts);
```

## Subscribing to a topic

You may subscribe to as many topics as you wish before entering the main loop. For each subscription you must provide the topic name, the callback function to handle the incoming events from that topic, and an optional array of parameters the the subscription:


```php
$broker->subscribe('/topic/path',$opts,"callbackUserFunc");
```

## Topic name

A topic is a namespace in the form of a filesystem path. Each topic is related to certain kind of events. Topic producers are advised to follow a logical hierarchy like this `/unit/platform/kind/subkind/...`

For instance, `/sapo/blogs/activity/post` updates every time a user from the SAPO Blogs platform posts an article, and `/sapo/blogs/activity/session` sends events each time a user logs on.

You can also subscribe to multiple topics with only one subscription using  regular expressions. For instance, subscribing to `/sapo/blogs/activity/.*` will get you events from both topics above, plus anything after `/sapo/blogs/activity/...`

## Subscription options

### destination_type

There are 3 supported types of destination:

* TOPIC (default)
* QUEUE
* TOPIC_AS_QUEUE

*TOPIC* type topics are fire and forget events. The produced events are broadcast across the cloud and one or more consumers can read those events simultaneously. This is actually the most common use-case and the default setting.

*QUEUE* type topics implement true persistent and secure queues across a broker agent cloud. Each produced event is guaranteed only to be read by one consumer and leaves the queue only when it's acknowledged by both ends. Use cases for queues are online payment events, a registration process, etc.

*TOPIC_AS_QUEUE* is very similar to the above but the events are produced as normal TOPIC types and fired and forgotten, only the consumers act like QUEUEers. This is very useful for load balancing scenarios. 

For instance, suppose you have a normal TOPIC type with loads of messages per second which you need to process into a SQL database. You can have 2 or more TOPIC_AS_QUEUE subscribed consumers processing those events and the cloud guarantees the equal distribution of unique events to each, so in practice you're distributing the topic load to N consumers. As there's no acknowledgement in the producer you can also mix TOPIC with TOPIC_AS_QUEUE subscriptions, and the cloud will handle this just fine.

## Callback function

This is the name of the function used to receive the payload from the events. This function will be called by the main loop each time a new event arrives and it's supposed to process the payload in any way to user wishes.

If the callback function is inside a class you can use this syntax:

```php
$broker->subscribe('/topic/path',$opts,array("Class_Name","callbackUserFunc"));
```

The function must take 3 parameters:

```php
function callbackFunc($payload,$topic,$message_id) {};
```

### payload

This includes the event payload which can be pretty much anything the producer defines, an XML message or plain text.

### topic

The topic the event refers to. This is useful if you're using wildcard subscriptions.

### message_id

Each event has a MessageId. This is it.

## Advanced usage

### Periodic calls

For flexibility you can add your own periodic calls to the main loop. They will be executed regardless of the incoming traffic in your subscribed topics. This allows more complex programs to be written with the SAPO_Broker APIs.

Just call `add_callback` as many times as you wish before the main loop, like this:

```php
$broker->add_callback(array("sec"=>5),"periodicCall");
```

Again, you can use the `array("Class_Name","periodicCall")` syntax if your function lives inside a class.

In the input arguments you can use `sec` (seconds) or `usec (microseconds) to define the time interval for which the function is executed.

## Examples

You can look at some examples <a href="https://github.com/sapo/sapo-broker/tree/master/clients/php-component/examples">here</a>.

----

# Using the Perl client

## Installing from source

<pre class="prettyprint">
git clone git@github.com:sapo/sapo-broker.git
cd sapo-broker/clients/perl-component && perl Makefile.PL
make install
</pre>

During build, must select at least one of the `thrift` or `protobuf` codecs, otherwise the `makefile` won't be created

## Dependencies

### Thrift

The build process should be similar to:

<pre class="prettyprint">
wget 'http://www.apache.org/dist//incubator/thrift/XXX-incubating/thrift-XXX.tar.gz'
tar -xzf thrift-XXX.tar.gz 
cd thrift-XXX/lib/perl/
perl Makefile.PL
#you may need to install dependencies from CPAN
make
sudo make install
</pre>

### Protobuf

Most distributions will have `protobuf` packages, but you can always compile and install from source as follows:

<pre class="prettyprint">
wget http://protobuf.googlecode.com/files/protobuf-XXX.tar.bz2
tar -xjf protobuf-XXX.tar.bz2
cd protobuf-XXX
./configure
make
sudo make install
</pre>

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

[repo]: https://github.com/sapo/sapo-broker
[ml]: http://listas.softwarelivre.sapo.pt/mailman/listinfo/broker
[bsd]: https://github.com/sapo/sapo-broker/blob/master/license/LICENSE.txt
[l]: https://github.com/sapo/sapo-broker/tree/master/license
[c]: https://github.com/sapo/sapo-broker/tree/master/clients/c-component
[net]: https://github.com/sapo/sapo-broker/tree/master/clients/dotnet-component
[o]: https://github.com/sapo/sapo-broker/tree/master/clients
[pl]: https://github.com/sapo/sapo-broker/tree/master/clients/perl-component)
[py]: https://github.com/sapo/sapo-broker/tree/master/clients/python-component)
[php]: https://github.com/sapo/sapo-broker/tree/master/clients/php-component)


