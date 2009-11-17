#!/usr/bin/env python

from Broker.Clients import Async as Client
from Broker.Transport import TCP, UDP

import logging
logging.basicConfig(level=logging.WARN)

server='localhost'
destination = '/python/tests'
destination_type = 'QUEUE'

broker = Client(TCP(host=server))

def callback(msg):
    print "%r\t%r" % (msg.destination, msg.message.payload)

broker.subscribe(destination, callback, destination_type)

broker.start()
broker.join()
