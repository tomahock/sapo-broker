---
layout: broker-documentation
title: Python
---

# Install

To install the python broker lib go to [GitHub](https://github.com/sapo/sapo-broker/tree/master/clients/python-component).
    

# Usage


## Connecting

```python
from Broker.Messages import Message, Subscribe, Acknowledge, Fault
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


```python

# .... connect to broker ....


destination = '/python/tests/expiration'
destination_type = 'QUEUE'

# and subscribe to something
broker.send(Subscribe(destination=destination, destination_type=destination_type))

while True:
    message = broker.receive() #blocks!!!
    
    payload = message.message.payload
    
    # ... do something with your payload ....  
    
    # acknowledge the message
    broker.send(Acknowledge(message_id=message.message.id, destination=message.subscription))

```

## Subscribing a topic

```python

# .... connect to broker ....


destination = '/python/tests/expiration'
destination_type = 'TOPIC'

# and subscribe to something
broker.send(Subscribe(destination=destination, destination_type=destination_type))

while True:
    message = broker.receive() #blocks!!!
    
    payload = message.message.payload
    
    # ... do something with your payload ....  

```

## Pool a message with timeout

  
```python

# .... connect to broker ....

destination = '/python/tests/'


while True:
    broker.send(Poll(destination=destination, timeout=5000))
    message = broker.receive() #blocks!!!
    
    if isinstance(message, Fault) and message.fault_code == '2005':
        print "timeout"
        break
        
        
    if isinstance(message, Fault)
        print 'another fault'
        break

    payload = message.message.payload
    
    # ... do something with your payload ....  
    
    # acknowledge the message
    broker.send(Acknowledge(message_id=message.message.id, destination=message.subscription))

```






