#
# Source: Dash docset for python3
#

import socketserver
import pickle
import sys


class BankState(object):
    # attribute: amount
    __slots__ = ('amount')
    
    def __init__(self, initial_amount = 0):
        self.amount = initial_amount
        
    def deposit(self, amount):
        print(f"{self.amount:4} > deposit({amount})")
        try:
            self.amount += amount
            return (True, self.amount)
        except:
            return (False, self.amount)
        
    def withdraw(self, amount):
        print(f"{self.amount:4} > withdraw({amount})")
        try:
            is_enough = self.amount - amount >= 0
            if is_enough:
                self.amount -= amount
            return (is_enough, self.amount)
        except:
            return (False, self.amount)
        
    def balance(self):
        print("{self.amount:4} > balance()")
        return (True, self.amount)



class BankServer(socketserver.BaseRequestHandler):
    """
    This class works similar to the TCP handler class, except that
    self.request consists of a pair of data and client socket, and since
    there is no connection, the client address must be given explicitly
    when sending data back via sendto().
    """

    bank = BankState(0)
    
    def handle(self):
        data     = pickle.loads(self.request[0])
        socket   = self.request[1]
        clt_addr = self.client_address[0]
        print(f"{clt_addr} sent: {data}")

        resp = (False, "")
        try:
            (command, argument) = data
            match command:
                case "DEPOSIT":  resp = self.bank.deposit(int(argument))
                case "WITHDRAW": resp = self.bank.withdraw(int(argument))
                case "BALANCE":  resp = self.bank.balance()
                case _: resp = (False, f"Unknown command: {command}")

        except BaseException as e:
            resp = (False, f"Error processing command: {e}")

        #
        # SEND response
        #
        print(f" -> {resp}")
        socket.sendto(pickle.dumps(resp), self.client_address)


if __name__ == "__main__":
    HOST, PORT = "localhost", 9999
    print('Python version:', sys.version)
    with socketserver.UDPServer((HOST, PORT), BankServer) as server:
        print(f'Server at {HOST}:{PORT}')
        print('Ready...')
        server.serve_forever()
        