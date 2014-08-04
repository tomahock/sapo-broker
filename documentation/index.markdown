---
layout: broker-documentation
title: Sapo Broker
site_root:  /
tags:
---


## Where to start


The most common use cases to implement using SAPO broker are :

- [Queuing Messaging Pattern (**QUEUE**)](#mqp)
- [Publish-Subscribe Pattern (**TOPIC**)](#pubsubp)
   
   

What usecase you should use really depends on your application needs.

If want to implement a Queuing Messaging Pattern you should use QUEUES to publish your messages,
because they guarantee :

- Message persistence 

       Messages are stored and delivered only when consumers are available.
           
- At-least-once Delivery

    Every message should be acknowledge to guarantee that the message was processed. 
    If the message is not acknowledge within a 2 minutes time frame then the message will be
      redelivered to any consumer on that queue. By default the delivery timeout is 2 minutes but if
      you need to change this time please consider the use of **POLL** operation instead of **SUBSCRIBE**. 
       
    

To have the same guarantees of a **QUEUE** when consuming from a **TOPIC** then you should consume 
from a **VIRTUAL QUEUE**. 
When you are consuming as a **VIRTUAL QUEUE** you are asking to broker agent to copy and store
all messages sent to that **TOPIC** and resend it to you as a **QUEUE**.
 

<div>
    <img  alt="Publish Subscribe" src="{{ site.url }}/broker/broker_client_decision.png" style="display: block;" class="push-center" />
</div>

## Use Cases


### <a name="pubsubp"></a>Queuing Messaging Pattern

<div>
    <img  alt="Queuing Messaging" src="{{ site.url }}/broker/loadbalance.png" style="display: block;" class="push-center" />
</div>
<br><br>

### <a name="mqp"></a>Publish-Subscribe Pattern

<div>
    <img alt="Publish Subscribe" src="{{ site.url }}/broker/pubsub.png" style="display: block;" class="push-center" />
</div>

