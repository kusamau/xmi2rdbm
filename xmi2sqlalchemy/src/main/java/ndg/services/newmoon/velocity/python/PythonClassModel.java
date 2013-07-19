/**
 * BSD Licence
 * Copyright (c) 2009, Science & Technology Facilities Council (STFC) All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   - Neither the name of the Science & Technology Facilities Council
 *     (STFC) nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package ndg.services.newmoon.velocity.python;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.collect.SuperAttribute;
import ndg.services.newmoon.velocity.python.support.ASCodeList;
import ndg.services.newmoon.velocity.python.support.ASEnumeration;
import ndg.services.newmoon.velocity.python.support.ASUnion;
import ndg.services.newmoon.xmiModel.UMLClass;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author mnagni
 *
 */
public class PythonClassModel extends ClassModel {
	
	/**
	 * @param associatedClass
	 */
	public PythonClassModel(ClassModel cm) {
		super(cm.getAssociatedClass());
		this.setStereotype(cm.getStereotype());
		this.getSimpleKeyValues().addAll(cm.getSimpleKeyValues());
		this.getParents().addAll(cm.getParents());
		this.getAttributeModel().addAll(cm.getAttributeModel());
	}
	 
	public boolean skipIt() {
		return this.getAssociatedClass().getClass().equals(ASEnumeration.class) 
				|| this.getAssociatedClass().getClass().equals(ASCodeList.class)
				|| this.getAssociatedClass().getClass().equals(ASUnion.class)
				|| !NmVelocityHelper.validateClass(this);
	}
	
	/**
	 * Returns the import for this module
	 */ 
	public Map<String, Set<String>>  generateImports() {
		PythonImportClosure closure = new PythonImportClosure();
		List<? super AttributeModel> toImport = new ArrayList();
		CollectionUtils.addAll(toImport, this.getParents().iterator());	
		
		//imports the inherited types
		//importInheritedTypes(this, toImport);
		
		CollectionUtils.addIgnoreNull(toImport, getStereotypeDummy(this.getStereotype()));
		
		if (this.isUnion()) {
			CollectionUtils.addAll(toImport, this.getAttributeModel().iterator());
		}		
		
//		for (AttributeModel am : this.getAttributeModel()) {
//			if (am instanceof SuperAttribute) {
//				toImport.add(am);
//			}
//		}
		
		CollectionUtils.forAllDo(toImport, closure);
		if (this.getAssociatedClass() != null 
				&& this.getAssociatedClass().getUmlPackage() != null
				&& closure.getImportMap().containsKey(NmVelocityHelper.getMappedModule(this.getAssociatedClass()))) {
			Set<String> classes = closure.getImportMap().get(NmVelocityHelper.getMappedModule(this.getAssociatedClass()));
			if (classes.contains(this.getAssociatedClass().getName())) {
				classes.remove(this.getAssociatedClass().getName());
			}
			if (classes.isEmpty()) {
				closure.getImportMap().remove(NmVelocityHelper.getMappedModule(this.getAssociatedClass()));
			}
		}

		return closure.getImportMap();
	}		
	
	public UMLClass getStereotypeImplementation() {
		return getStereotypeDummy(this.getStereotype());
	}
	
	/**
	 * Checks if this instance redefines a parent attribute. 
	 * */
	public Set<PythonAttributeModel> getAttributes(){
		Set<PythonAttributeModel> attributes = new HashSet<PythonAttributeModel>();
		extractAttributes(this.getAttributeModel(), attributes, null);
		for (ClassModel grandfather : this.getParents()) {
			extractAttributes(grandfather.getAttributeModel(), attributes, grandfather);
			extractParentAttributes(grandfather, attributes, grandfather);
		}		
		return attributes;
	}

	private void extractParentAttributes(ClassModel cm, Set<PythonAttributeModel> attributes, ClassModel parent) {
		extractAttributes(cm.getAttributeModel(), attributes, parent);
		for (ClassModel grandfather : cm.getParents()) {
			extractParentAttributes(grandfather, attributes, parent);			
		}		
	}
	
	private void extractAttributes(Set<AttributeModel> am, Set<PythonAttributeModel> pa, ClassModel owner) {
		for (AttributeModel attribute : am) {
			CollectionUtils.addIgnoreNull(pa, generatePythonAttribute(attribute, owner));
		}		
	}
	
	private PythonAttributeModel generatePythonAttribute(AttributeModel am, ClassModel owner) {
		
		if (am == null 
				|| am.getAssociatedType() == null
				|| am.getAssociatedType().getAssociatedClass() == null
				|| am.getAssociatedType().getAssociatedClass().getName() == null)
			return null;
		String name = am.getAssociatedType().getAssociatedClass().getName();
		String[] imp = NmVelocityHelper.formatPythonImportString(am.getAssociatedType().getAssociatedClass());
		if (imp != null) {
			name = imp[1];
		}

		if (am instanceof SuperAttribute) {
			return new PythonSuperAttributeModel((SuperAttribute)am, owner, skipIt() ? null : name);
		} else {
			return new PythonAttributeModel(am, owner, skipIt() ? null : name);
		}
	}
	
	public static UMLClass getStereotypeDummy(STEREOTYPE stereotype) {
		if (stereotype == null)
			return null;
		
		switch (stereotype) {
		case Enumeration:
			return new ASEnumeration();
		case CodeList:
			return new ASCodeList();
		case Union:
			return new ASUnion();			
		default:
			break;
		}
		return null;
	}
}
