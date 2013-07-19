'''
Created on Nov 25, 2011

@author: mnagni
'''
import inspect
from copy import deepcopy

class UnionMeta(type):

    def __init__(cls, classname, bases, dict_):
        for k, v in dict_.items():
            if k.startswith('_'):
                continue
            
            if isinstance(v, dict):
                values = {}
                for attrName, attrType in v.items():          
                    if not inspect.isclass(attrType):
                        raise Exception("Types %s in not a class" % (attrType))
                    else:
                        values[attrName] = None
                setattr(cls, '_values', values)
                setattr(cls, '_typesDictionary', v)
        return type.__init__(cls, classname, bases, dict_)

class ASUnion(object):
    __metaclass__ = UnionMeta
    
    '''
        Implements the GML <<Union>> stereotype.
        Extract from the OGC 07-036:
            <<Union>> is a set of properties. 
            The semantics is that only one of the properties may be present at any time.

        Because of this this class has two inner dictionaries, 
            - a dictionary of pairs attributeName:classType
            - a dictionary of pairs attributeName:value
            
        The attributes are managed by one get/set method for the actual value
        and one getTypes() to enlist which kind of instances are accepted by the class
    '''    
    
    def getValue(self, attributeName):
        '''
        @param attributeName: the name of the attribute to get
        @raise KeyError: if the given attributeName does not exist
        '''       
        self._attributeHasType (attributeName)
        return self._values[attributeName]
        
    def setValue(self, attributeName, value):
        '''
        @param attributeName: the name of the attribute to set        
        @param value: the new instance value 
        @raise KeyError: if the given attributeName does not exist
        @raise TypeError: if the given value does not match the attribute type
        '''
        
        attrType = self._attributeHasType (attributeName)
        
        if value is None:
            self._values[attributeName] = value
            
        #check that the given value is
        try:
            self._checkValues(value, attrType)
        except TypeError :
            raise TypeError("The value (or values) type does not match the attribute %s type" % (attributeName))
                
        self._values[attributeName] = value
    
    def getAttributesDictionary(self):
        return deepcopy(self._typesDictionary)

    def _attributeHasType(self, attributeName):
        '''
        @param attributeName: the name of the attribute to get
        @raise KeyError: if the given attributeName does not exist
        '''          
        try:
            attrType = self._typesDictionary[attributeName]    
        except Exception as ex:
            raise KeyError("The attribute named %s does not exist" % (attributeName))
        return attrType
    
    def _checkValues(self, value, type):
        if isinstance(value, list):
            for v in value:
                self._checkType(v, type)
        else:
            self._checkType(value, type)
    
    def _checkType(self, value, innerType):
        if type(value) != innerType:
            raise TypeError("The value type does not match the attribute type %s" % (type))
'''    
class MyUnion(ASUnion):
    test = {'attr1': AssertionError, 'attr2': ArithmeticError}
        
mu = MyUnion()
print(mu.getAttributesDictionary())

#Raises exception
try:
    mu.setValue('attr1', TypeError('NOK'))
except Exception as ex:
    print ex

#Raises exception
try:
    mu.setValue('attr6', TypeError('NOK'))
except Exception as ex:
    print ex

#Does NOT raise exception
mu.setValue('attr1', AssertionError('OK'))

#Raises exception
try:
    print(mu.getValue('attr5'))
except Exception as ex:
    print ex
    
val = mu.getValue('attr1')
print(val)
'''