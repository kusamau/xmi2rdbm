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
package ndg.services.newmoon.collect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ndg.services.newmoon.velocity.python.support.ExportModel;
import ndg.services.newmoon.velocity.python.support.SimpleKeyValue;
import ndg.services.newmoon.xmiModel.UMLClass;

/**
 * @author mnagni
 *
 */
public class ClassModel implements ExportModel, Comparable<ClassModel> {
	public enum STEREOTYPE {
		Enumeration, CodeList, Union, DataType, Boolean, None, Metaclass, Type;
	}
	private final UMLClass associatedClass;
	private STEREOTYPE stereotype = STEREOTYPE.None;
	private final List<SimpleKeyValue> simpleKeyValues = new ArrayList<SimpleKeyValue>();
	private final Set<ClassModel> parents = new HashSet<ClassModel>();
	private final Set<AttributeModel> attributeModel = new HashSet<AttributeModel>();
	
	/**
	 * @param associatedClass
	 */
	public ClassModel(UMLClass associatedClass) {
		super();
		this.associatedClass = associatedClass;
	}
	
	public boolean isEnumeration() {
		return stereotype.equals(STEREOTYPE.Enumeration);
	}
	
	public boolean isCodeList() {
		return stereotype.equals(STEREOTYPE.CodeList);
	}
	
	public boolean isUnion() {
		return stereotype.equals(STEREOTYPE.Union);
	}
	
	/**
	 * @return the associatedClass
	 */
	public UMLClass getAssociatedClass() {
		return associatedClass;
	}	
	
	/**
	 * @return the stereotype
	 */
	public STEREOTYPE getStereotype() {
		return stereotype;
	}

	/**
	 * @param stereotype the stereotype to set
	 */
	public void setStereotype(STEREOTYPE stereotype) {
		this.stereotype = stereotype;
	}

	/**
	 * @return the simpleKeyValues
	 */
	public List<SimpleKeyValue> getSimpleKeyValues() {
		return simpleKeyValues;
	}

	/**
	 * @return the parents
	 */
	public Set<ClassModel> getParents() {
		return parents;
	}

	/**
	 * @return the attributeModel
	 */
	public Set<AttributeModel> getAttributeModel() {
		return attributeModel;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((associatedClass == null) ? 0 : associatedClass.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassModel other = (ClassModel) obj;
		if (associatedClass == null) {
			if (other.associatedClass != null)
				return false;
		} else if (!associatedClass.equals(other.associatedClass))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClassModel [associatedClass=" + associatedClass + "]";
	}

	/**
	 * Compares two {@link ClassModel}s.
	 * @param other the <code>ClassModel<code> to compare
	 * @return 1 if this ClassModel has already <code>other</code> among
	 * its parents otherwise -1 
	 */
	@Override
	public int compareTo(ClassModel other) {
		Set<ClassModel> cms = getAllParents(this, null);
		return cms.contains(other) ? -1 : 1;
	}			
	
	private Set<ClassModel> getAllParents(ClassModel cm, Set<ClassModel> cms) {
		if (cms == null) {
			cms = new HashSet<ClassModel>();
		}		
		for (ClassModel parent : cm.getParents()) {
			cms.add(parent);
			getAllParents(parent, cms);
		}
		return cms;
	}
}
