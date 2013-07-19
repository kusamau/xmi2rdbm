'''
Created on 5 Oct 2011

@author: mnagni
'''
from sqlalchemy.types import SchemaType, TypeDecorator, Enum
import re

class ASEnumType(SchemaType, TypeDecorator):
    def __init__(self, enum, regex = None):        
        self.enum = enum
        self.impl = Enum(
                        *enum.values(),
                        name="ck%s" % re.sub(
                                    '([A-Z])',
                                    lambda m:"_" + m.group(1).lower(),
                                    enum.__name__)
                    )
        self.regex = regex

    def _set_table(self, table, column):
        self.impl._set_table(table, column)

    def copy(self):
        return ASEnumType(self.enum)

    def process_bind_param(self, value, dialect):
        if value is None:
            return None
        if self.__checkValue(value):
            return value.value

        return None

    def process_result_value(self, value, dialect):
        if value is None:
            return None
        if self.__checkValue(self.enum.from_string(value.strip())):
            return self.enum.from_string(value.strip())

        return None

    def __checkValue(self, value):
        if self.regex is not None:
            if (re.match(self.regex, value)):
                return value
        if value in self.enum:
            return True
        
        return False;


class EnumSymbol(object):
    """Define a fixed symbol tied to a parent class."""

    def __init__(self, cls_, name, value, description = None):
        self._cls = cls_
        self.name = name
        self.value = value
        self.description = description

    def __reduce__(self):
        """Allow unpickling to return the symbol
        linked to the DeclEnum class."""
        return getattr, (self._cls, self.name)

    def __iter__(self):
        return iter([self.value, self.description])

    def __repr__(self):
        return "<%s>" % self.name

class EnumMeta(type):

    def __init__(cls, classname, bases, dict_):
        cls._reg = reg = cls._reg.copy()
        for k, v in dict_.items():
            if k.startswith('_'):
                continue
            
            if isinstance(v, tuple):
                sym = reg[v[0]] = EnumSymbol(cls, k, *v)
                setattr(cls, k, sym)
            elif isinstance(v, str):
                sym = reg[v] = EnumSymbol(cls, k, v)
                setattr(cls, k, sym)
        return type.__init__(cls, classname, bases, dict_)

    def __iter__(self):
        return iter(self._reg.values())

class ASEnumeration(object):
    """Declarative enumeration."""

    __metaclass__ = EnumMeta
    _reg = {}

    @classmethod
    def from_string(cls, value):
        try:
            return cls._reg[value]
        except KeyError:
            raise ValueError(
                    "Invalid value for %r: %r" %
                    (cls.__name__, value)
                )

    @classmethod
    def values(cls):
        return cls._reg.keys()
    
    @classmethod
    def db_type(cls):
        return ASEnumType(cls)
    
'''
class EmployeeType(ASEnumeration):
    part_time = "part_time", "Part Time"
    full_time = "full_time", "Full Time"
    contractor = "contractor", "Contractor"

emp = EmployeeType()
print (emp)    
'''    