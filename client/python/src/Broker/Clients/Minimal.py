from __future__ import with_statement #for python 2.5

class DummyLock:
    def __enter__(self):
        return True

    def __exit__(self, type, value, traceback):
        pass

class Client:
    '''minimalistic client for Broker'''

    def __init__(self, codec, transport, lock=None):
        self.__codec = codec
        self.__transport = transport

        if lock is None:
            lock = DummyLock()

        self.__lock = lock

    def send(self, message):
        with self.__lock:
            serialized = self.__codec.serialize(message)

        with self.__lock:
            self.__transport.send(serialized)

        return self

    def receive(self):
        with self.__lock:
            data = self.__transport.receive()
        with self.__lock:
            return self.__codec.deserialize(data.payload)
