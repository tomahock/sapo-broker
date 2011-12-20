from INET import Transport as INET

import socket
import logging

LOG = logging.getLogger('Broker.Transports.TCP')

DEFAULT_PORT = 3323
DEFAULT_HOST = 'localhost'

class Transport(INET):

    socket_type = socket.SOCK_STREAM

    def __init__(self, host=DEFAULT_HOST, port=DEFAULT_PORT, timeout=None):
        INET.__init__(self, host, port, timeout)

        try:
            #try some basic socket options
            #failure should be OK
            self.__socket.setsockopt(socket.SOL_TCP,socket.SO_KEEPALIVE,True)
            self.__socket.setsockopt(socket.SOL_SOCKET,socket.SO_KEEPALIVE,True)
            #10 seconds of idle connection time
            self.__socket.setsockopt(socket.SOL_TCP,socket.TCP_KEEPIDLE, 10000)
            #see also  SOL_TCP integer parameters TCP_KEEPIDLE, TCP_KEEPINTVL,and TCP_KEEPCNT
        except Exception, ex:
            LOG.warning("Socket options failed. %s", ex)

