#!/usr/bin/env python

from Broker.Messages import Message, Publish
from Broker.Transport import TCP, UDP
from Broker.Codecs import Codec #auto codec selection (thrift or protobuf if thrift isn't installed)
from Broker.Clients import Minimal

server='localhost'
destination = '/python/tests'
destination_type = 'QUEUE'
N=10000

broker = Minimal(codec=Codec(), transport=TCP(host=server))

for n in xrange(N):
    message = Message(payload='Message number %d' % n)
    publish = Publish(destination=destination, destination_type=destination_type, message=message)
    broker.send(publish)
