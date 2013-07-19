'''
Created on 5 Dec 2011

@author: mnagni
'''
from sqlalchemy.types import TypeDecorator, TEXT

class ASText(TypeDecorator):
    '''
        Some ISO19103 primitive could be associated simply to an SQL type.
    '''    
    impl = TEXT

    def process_bind_param(self, value, dialect):
        return value

    def process_result_value(self, value, dialect):
        return value
