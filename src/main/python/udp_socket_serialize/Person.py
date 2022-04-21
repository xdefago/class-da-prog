
class Person(object):
    def __init__(self, name, age):
        self.name = name
        self.age  = age
        
    def toString(self):
        return 'Person('+self.name+','+self.age+')'
    