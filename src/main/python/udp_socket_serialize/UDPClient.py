#
# Source: Dash docset for python3
#

import socket
import sys
import pickle

HOST, PORT = "localhost", 9999

data = list(map(int, input('Enter list of numbers: ').split()))

print(data)

# SOCK_DGRAM is the socket type to use for UDP sockets
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# As you can see, there is no connect() call; UDP has no connections.
# Instead, data is directly sent to the recipient via sendto().
sock.sendto(pickle.dumps(data), (HOST, PORT))
received = pickle.loads(sock.recv(1024))

print("Sent:     {}".format(data))
print("Received: {}".format(received))
