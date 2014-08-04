---
layout: broker-documentation
title: Perl
---

# Install

To install the python broker lib go to [GitHub](https://github.com/sapo/sapo-broker/tree/master/clients/python-component).
    

# Usage


## Connecting

```python
from Broker.Messages import Message, Subscribe, Acknowledge
from Broker.Transport import TCP, UDP
from Broker.Codecs import Codec #auto codec selection (thrift or protobuf if thrift isn't installed)
from Broker.Clients import Minimal

server='broker.bk.sapo.pt'


broker = Minimal(codec=Codec(), transport=TCP(host=server))
```

## Sending Message to a Topic or QUEUE

```python

# .... connect to broker ....

server='broker.bk.sapo.pt'
destination = '/python/tests/expiration'
destination_type = 'QUEUE' #Or TOPIC


payload_msg = 'Message' 
message = Message(payload=payload_msg)

publish = Publish(destination=destination, destination_type=destination_type, message=message)

broker.send(publish)

 
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




