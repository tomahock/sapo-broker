---
layout: cookbook
title: Sapo Broker
site_root:  /
tags:
---


sapo Broker is a distributed messaging framework. Among other features, it provides minimal administration overhead, Publish-Subscribe and Point-to-Point messaging, guaranteed delivery and wildcard subscriptions.

sapo Broker is written in Java, but has client libraries for Perl, Python, PHP, .NET, C and quite a few others, and you can talk to it using Thrift, XML or JSON.

To start using sapo Broker take a look at the Quick Start tutorial, and then please read the User Guide for more in depth information.

You can access our public broker server at broker.labs.sapo.pt and subscribe to a number of public topics to experiment with.


## How do I get started?

Download the latest distribution bundle and read the Quick Start. To learn more about the design and concepts of SAPO Broker read the User Guide.

You can fetch the latest version of the SAPO Broker source code from our Git repo. For checking out the code use the <code>git</code> command line client as follows:

```bash
    git clone git@github.com:sapo/sapo-broker.git
```


## Arquitecture 

![Sapo Broker Arquitecture](/broker/sapobroker_stack.png)


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