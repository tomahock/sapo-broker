#!/usr/bin/env python

from Broker.Messages import Message, Subscribe, Acknowledge
from Broker.Transport import TCP, UDP
from Broker.Codecs import Protobuf
from Broker.Clients import Minimal

server='localhost'
destination = '/python/tests'
destination_type = 'QUEUE'
N=10000

broker = Minimal(codec=Protobuf(), transport=TCP(host=server))

broker.send(Subscribe(destination=destination, destination_type=destination_type))
for n in xrange(N):
    message = broker.receive()
    if destination_type == 'QUEUE':
        #acknowledge
        broker.send(Acknowledge(message_id=message.message.id, destination=destination))
