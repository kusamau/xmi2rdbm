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
package ndg.services.newmoon.velocity.python.support;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.xmiModel.UMLClass;

/**
 * @author mnagni
 *
 */
public class TableModel implements ExportModel, Comparable<TableModel>{
	private final ClassModel associatedClassModel;
	private final Set<AttributeModel> attributeModel = new HashSet<AttributeModel>();	
	private final Set<ForeignKey> fk = new HashSet<ForeignKey>();
	private final Set<Relationship> relationship = new HashSet<Relationship>();
	private final Set<TableModel> inherited = new HashSet<TableModel>();
	private final Set<OverriddenAttribute> oa = new HashSet<OverriddenAttribute>();
	//Defines if this tables is inherited from one or more other tables
	private boolean hasChildren = false;

	/**
	 * Sets skipIt as <code>false</code> by default. Because the {@link #equals(Object)}
	 * method use only the {@link #getAssociatedClass()} value this constructor
	 * can be used as dummy id to scan a <code>Collection\<TableModel\></code>  
	 * */
	public TableModel(ClassModel associatedClassModel) {
		this.associatedClassModel = associatedClassModel;
	}	
	
	/**
	 * @return the associatedClassModel
	 */
	public ClassModel getAssociatedClassModel() {
		return associatedClassModel;
	}



	/**
	 * @return the hasChildren
	 */
	public boolean isHasChildren() {
		return hasChildren;
	}

	/**
	 * @param hasChildren the hasChildren to set
	 */
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	/**
	 * @return the associatedClass
	 */
	public UMLClass getAssociatedClass() {
		return associatedClassModel.getAssociatedClass();
	}



	/**
	 * @return the am
	 */
	public Set<AttributeModel> getAttributeModel() {
		return attributeModel;
	}



	/**
	 * @return the fk
	 */
	public Set<ForeignKey> getFk() {
		return fk;
	}



	/**
	 * @return the relationship
	 */
	public Set<Relationship> getRelationship() {
		return relationship;
	}



	/**
	 * @return the inherited
	 */
	public Set<TableModel> getInherited() {
		return inherited;
	}

	/**
	 * @return the oa
	 */
	public Set<OverriddenAttribute> getOa() {
		return oa;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAssociatedClass() == null) ? 0 : getAssociatedClass().hashCode());
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
		TableModel other = (TableModel) obj;
		if (getAssociatedClass() == null) {
			if (other.getAssociatedClass() != null)
				return false;
		} else if (!getAssociatedClass().equals(other.getAssociatedClass()))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TableModel [associatedClass=" + getAssociatedClass() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TableModel o) {		
		Iterator<Relationship> relation = this.relationship.iterator();
		Relationship rl = null;
			while (relation.hasNext()) {
				rl = relation.next();
				if (rl.getToTable().equals(o) && !rl.getToTable().equals(rl.getFromTable()))
					return 1;
			}
		return -1;
	}		
}
