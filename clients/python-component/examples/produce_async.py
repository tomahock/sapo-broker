#!/usr/bin/env python

from Broker.Clients import Async as Client
from Broker.Transport import TCP, UDP

import logging
logging.basicConfig(level=logging.DEBUG)

server='localhost'
destination = '/python/tests'
destination_type = 'QUEUE'
N=10000

broker = Client(TCP(host=server))

broker.start()

for n in xrange(N):
    payload='Message number %d' % n
    broker.publish(destination=destination, destination_type=destination_type, payload=payload)

#broker.join()
