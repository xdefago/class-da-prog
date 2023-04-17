#
# Source: Dash docset for python3
#

import socket
import sys
import pickle


HOST, PORT = "localhost", 9999

# SOCK_DGRAM is the socket type to use for UDP sockets
#sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
    
    while True:
        #
        # User input
        #
        print('Command [DEPOSIT, WITHDRAW, BALANCE, END]')
        request = input('> ').split()
        
        if not request or request[0].upper() == "END":
            break
        elif request[0].upper() == "BALANCE":
            request.append("placeholder")
        
        #
        # SEND request
        #
        sock.sendto(pickle.dumps(request), (HOST, PORT))
        
        #
        # RECEIVE response
        #
        resp = pickle.loads(sock.recv(1024))
        
        print(f"  -> {resp}")

print("Goodbye!")
        