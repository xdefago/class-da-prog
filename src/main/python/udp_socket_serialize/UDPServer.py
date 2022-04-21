#
# Source: Dash docset for python3
#

import socketserver
import pickle

class MyUDPHandler(socketserver.BaseRequestHandler):
    """
    This class works similar to the TCP handler class, except that
    self.request consists of a pair of data and client socket, and since
    there is no connection the client address must be given explicitly
    when sending data back via sendto().
    """
    
    def handle(self):
        data   = pickle.loads(self.request[0])
        socket = self.request[1]
        print("{} wrote:".format(self.client_address[0]))
        print(data)
        resp = sum(data)
        print('->', resp)
        socket.sendto(pickle.dumps(resp), self.client_address)
        
if __name__ == "__main__":
    HOST, PORT = "localhost", 9999
    with socketserver.UDPServer((HOST, PORT), MyUDPHandler) as server:
        print('Listening...')
        server.serve_forever()
        
        