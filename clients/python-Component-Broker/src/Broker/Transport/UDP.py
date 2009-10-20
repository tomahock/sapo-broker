from INET import Transport as INET

DEFAULT_PORT=3323
DEFAULT_HOST='localhost'

import socket
import logging
LOG = logging.getLogger('Broker.Transports.UDP')

class Transport(INET):

    socket_type = socket.SOCK_DGRAM

    def __init__(self, host=DEFAULT_HOST, port=DEFAULT_PORT):
        INET.__init__(self, host, port)
