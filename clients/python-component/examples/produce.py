#!/usr/bin/env python

from Broker.Messages import Message, Subscribe, Acknowledge
from Broker.Transport import TCP, UDP, SSL
from Broker.Codecs import Protobuf, Thrift
from Broker.Clients import Minimal

#server='pesquisa1.pesquisa.bk.sapo.pt'
server='10.135.6.12'
destination = '.*pesquisa.*'
destination_type = 'TOPIC'
N=10000

broker = Minimal(codec=Thrift(), transport=TCP(host=server))

broker.send(Subscribe(destination=destination, destination_type=destination_type))
#for n in xrange(N):
try:
    while True:
        message = broker.receive()
        #broker.send(Acknowledge(message_id=message.message.id, destination=message.destination))
        #print message.message.payload
        #print message.destination
finally:
    print message.destination
