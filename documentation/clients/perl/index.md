---
layout: broker-documentation
title: Perl
---

# Install

To install the perl broker lib go to [GitHub](https://github.com/sapo/sapo-broker/tree/master/clients/perl-component).
    

# Usage


## Connecting

```perl
use SAPO::Broker::Clients::Simple;
        
use strict;
use warnings;
        
# connects to localhost using tcp by default (can also use udp or ssl)
my $broker = SAPO::Broker::Clients::Simple->new(
        host=>'localhost',
        port  => 3323,
        codec => 'protobufxs',
        proto=>'tcp',
); 
```

## Sending Message to a Topic or QUEUE

```perl

# .... connect to broker ....

my %options = (
    'destination_type' => 'TOPIC', #can also be QUEUE
    'destination' => '/tests/perl',
);

# now publish something
$broker->publish(%options, 'payload' => "This is the payload");
 
```


## Subscribing a Queue


```perl

# .... connect to broker ....


my %options = (
    'destination_type' => 'QUEUE', #can also be TOPIC
    'destination' => '/tests/perl',
);

# and subscribe to something
$broker->subscribe(%options);

while(1){

    my $notification = $broker->receive; # blocks!!!!
    
    my $payload = $notification->message->payload;
    
    # ... do something with your payload ....  
    
    # acknowledge the message
    $broker->acknowledge($notification);

}


 
```

## Subscribing a topic

```perl

# .... connect to broker ....


my %options = (
    'destination_type' => 'TOPIC',
    'destination' => '/tests/perl',
);

# and subscribe to something
$broker->subscribe(%options);

while(1){

    my $notification = $broker->receive; # blocks!!!!
    
    my $payload = $notification->message->payload;
    
    # ... do something with your payload ....  
    
}

 
```

## Pool a message with timeout

  
```perl

# .... connect to broker ....


my %options = (
    'destination_type' => 'QUEUE',
    'destination' => '/tests/perl',
    'timeout' => 1000,
);


while(1){

     $broker->poll(%options);
   
     my $notification;
       
     eval {
           $notification  = $broker->receive; # blocks!!!!
     };
   
     if($@){
   
           my $fault = $@;
   
           if( ref($fault) eq "SAPO::Broker::Messages::Fault" &&  $fault->fault_code == 2005 ){
           
               # .... Timeout.....
               print "timeout\n";
   
           }else{
           
               print $fault;
               
           }
   
           die;
     }
   
   
     my $payload = $notification->message->payload;
   
     # ... do something with your payload ....
     print( $payload );
   
   
     # acknowledge the message
     $broker->acknowledge($notification);

}

 
```


## Pool a message with a higher redelivery timeout

  
```perl

# .... connect to broker ....


my %options = (
    'destination_type' => 'QUEUE',
    'destination' => '/tests/perl',
    'header' => {
        'RESERVE_TIME' => 900000, # The message will be resent to any client after 15 minutes 
    }
);

while(1){

     $broker->poll(%options);
   
     my $notification;
       
     eval {
           $notification  = $broker->receive; # blocks!!!!
     };
   
     if($@){
         print $fault;
         die;
     }
   
     my $payload = $notification->message->payload;
   
     # ... do something with your payload ....
     # if your code takes more then 15 min (900000 miliseconds) then the message will be sent to another consumer


     # acknowledge the message
     $broker->acknowledge($notification);

}
```




