from __future__ import with_statement #for python 2.5

from Broker.Messages import Message, Subscribe, Unsubscribe, Acknowledge, Publish, Acknowledge
from Broker.Transport import TCP, UDP
from Broker.Clients import Minimal

import threading
from Queue import Queue

import logging

#Try to use the fastest serializer possible
try:
    from Broker.Codecs import Thrift as Codec
except ImportError:
    try:
        from Broker.Codecs import Protobuf as Codec
    except ImportError:
        raise NotImplementedError('No protocol serializer found')

class Client(threading.Thread):
    '''Basic client for Broker'''

    def __init__(self, transport, codec=Codec()):
        self.__log = logging.getLogger("Broker")
        threading.Thread.__init__(self)
        self.__minimal = Minimal(codec=codec, transport=transport)
        self.__rqueue = Queue(1)
        self.__wqueue = Queue(1)
        self.__callback_lock = threading.RLock()

        self.__callbacks = {
            'TOPIC' : {},
            'QUEUE' : {},
        }

        def __rloop():
            while True:
                self.__log.debug('__rloop reading')
                message = self.__minimal.receive()
                self.__log.debug('__rloop read %r', message)
                self.__rqueue.put(message)

        def __wloop():
            while True:
                self.__log.debug('__wloop reading')
                message = self.__wqueue.get()
                self.__log.debug('__wloop read %r', message)
                self.__minimal.send(message)
                self.__log.debug('__wloop sent %r', message)
                self.__wqueue.task_done()
        
        self.__rthread = threading.Thread(target=__rloop, name='broker_r')
        self.__rthread.setDaemon(True)

        self.__wthread = threading.Thread(target=__wloop, name='broker_w')
        self.__wthread.setDaemon(True)
        
        #start threads
        self.__log.debug('Starting threads')
        self.__rthread.start()
        self.__wthread.start()
        self.__log.debug('Threads started')

    def send(self, message, block=True, timeout=None):
        return self.__wqueue.put(message, block, timeout)

    def receive(self, block=True, timeout=None):
        msg = self.__rqueue.get(block=True, timeout=None)
        self.__rqueue.task_done()
        return msg

    def subscribe(self, destination, callback, destination_type='TOPIC', auto_acknowledge=True):
        self.__log.debug('subscribe(%r, %r, %r, %r)', destination, callback, destination_type, auto_acknowledge)
        
        with self.__callback_lock:
            assert(destination not in self.__callbacks[destination_type])

        if 'QUEUE' == destination_type and auto_acknowledge:
            #I hate python's scope semantics
            orig_callback = callback
            def auto_ack_callback(msg):
                #should I ack before or after processing?
                self.acknowledge(msg)
                orig_callback(msg)

            callback = auto_ack_callback

        with self.__callback_lock:
            self.__callbacks[destination_type][destination] = callback

        return self.send(Subscribe(destination=destination, destination_type=destination_type))

    def unsubscribe(self, destination, destination_type='TOPIC'):
        self.__log.debug('subscribe(%r, %r)', destination, destination_type)

        with self.__callback_lock:
            del self.__callbacks[destination_type][destination]

        return self.send(Unsubscribe(destination, destination_type))

    def publish(self, payload, destination, destination_type='TOPIC', expiration=None):
        message = Message(payload=payload, expiration=expiration)

        self.__log.debug('publish(%r, %r, %r, %r)', message, destination, destination_type, expiration)
        return self.send(Publish(message, destination, destination_type))

    def acknowledge(self, message):
        self.__log.debug('acknowledge(%r)', message)
        return self.send(Acknowledge(message_id=message.message.id, destination=message.destination))

    def run_once(self, block=True):
        self.__log.debug('loop_read_once(block=%r)', block)
        msg = self.receive(block)
        self.__log.debug('read message %r', msg)

        try:
            callback = self.__callbacks[msg.destination_type][msg.subscription]
        except KeyError:
            raise KeyError('No handler for %s:%s' % (msg.destination_type, msg.subscription))
    
        callback(msg)

    def run(self):
        while True:
            self.run_once()
