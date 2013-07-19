'''
Created on 5 Oct 2011

@author: mnagni
'''
from ascore.asenumeration import ASEnumeration
class ASCodeList(ASEnumeration):
    def __init__(self, regex):
        self.regex = 'other: \w{2,}'
        ASEnumeration.__init__(self)  

'''
class EmployeeType(ASCodeList):
    part_time = "part_time", "Part Time"
    full_time = "full_time", "Full Time"
    contractor = "contractor", "Contractor"
    
type = Column(EmployeeType.db_type())
    
employee = Employee(name, EmployeeType.part_time)    
'''