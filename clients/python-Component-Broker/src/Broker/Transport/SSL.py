from TCP import Transport as TCP

import logging
import ssl

LOG = logging.getLogger('Broker.Transports.TCP')
DEFAULT_PORT = 3390
DEFAULT_HOST = 'localhost'

class Transport(TCP):

    def __init__(self, host=DEFAULT_HOST, port=DEFAULT_PORT):
        TCP.__init__(self, host, port)

        #now try to warp the socket into SSL stuf
        self.__socket = ssl.wrap_socket(self.__socket)
        #ssl doesn't have a recv method so emulate it
        self.__socket.recv = self.__socket.read
