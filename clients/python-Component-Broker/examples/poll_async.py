#!/usr/bin/env python

from Broker.Clients import Async as Client
from Broker.Transport import TCP, UDP

import logging
logging.basicConfig(level=logging.WARNING)

server='localhost'
destination = '/python/tests'
destination_type = 'QUEUE'
N=10000

broker = Client(TCP(host=server))

def callback_factory(n):

    def callback(msg):
        print "%d\t%r\t%r" % (n, msg.destination, msg.message.payload)

    return callback

broker.start()
for n in xrange(N):
    broker.poll(destination, callback_factory(n))

broker.join()
