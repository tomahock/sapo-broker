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

    def callback():
        print "Pong for %d" % (n,)

    return callback

broker.start()
for n in xrange(N):
    broker.ping(callback_factory(n))

broker.join()
