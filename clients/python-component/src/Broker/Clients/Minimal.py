class Client:
    '''minimalistic client for Broker'''

    def __init__(self, codec, transport):
        self.__codec = codec
        self.__transport = transport

    def send(self, message):
        serialized = self.__codec.serialize(message)
        self.__transport.send(serialized)
        return self

    def receive(self):
        data = self.__transport.receive()
        return self.__codec.deserialize(data.payload)
