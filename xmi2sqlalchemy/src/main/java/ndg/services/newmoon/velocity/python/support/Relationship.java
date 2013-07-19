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


/**
 * @author mnagni
 *
 */
public class Relationship {
	public enum RELATION_TYPE {ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY}
	
	private final TableModel fromTable;
	private final TableModel toTable;	
	private final String name;
	private final boolean nullable;
	private RELATION_TYPE relationType = null;
	private String backrefName = null;
	//If true indicates that even if the relationType is MANY-to-MANY
	//the relation is a ONE-to-ONE relation.
	private final boolean useList;
	private ForeignKey associatedForeignKey  = null;
	private AssociationTable associationTable  = null;
	
	/**
	 * @param fromTable
	 * @param toTable
	 * @param name
	 * @param nullable
	 */
	public Relationship(TableModel fromTable, TableModel toTable, String name, boolean nullable, boolean useList) {
		super();
		this.fromTable = fromTable;		
		this.toTable = toTable;
		this.name = name;
		this.nullable = nullable;
		this.useList = useList;		
	}
	
	/**
	 * @return the associationTable
	 */
	public AssociationTable getAssociationTable() {
		return associationTable;
	}



	/**
	 * @param associationTable the associationTable to set
	 */
	public void setAssociationTable(AssociationTable associationTable) {
		this.associationTable = associationTable;
	}



	/**
	 * @return the associatedForeignKey
	 */
	public ForeignKey getAssociatedForeignKey() {
		return associatedForeignKey;
	}

	/**
	 * @param associatedForeignKey the associatedForeignKey to set
	 */
	public void setAssociatedForeignKey(ForeignKey associatedForeignKey) {
		this.associatedForeignKey = associatedForeignKey;
	}



	/**
	 * @return the useList
	 */
	public boolean isUseList() {
		return useList;
	}



	public boolean isManyToMany() {
		return checkRelationType(RELATION_TYPE.MANY_TO_MANY);
	}

	public boolean isOneToMany() {
		return checkRelationType(RELATION_TYPE.ONE_TO_MANY);
	}
	
	public boolean isManyToOne() {
		return checkRelationType(RELATION_TYPE.MANY_TO_ONE);
	}
	
	public boolean isOneToOne() {
		return checkRelationType(RELATION_TYPE.ONE_TO_ONE);
	}
	
	private boolean checkRelationType(RELATION_TYPE type) {
		return relationType == null ? false : relationType.equals(type);
	}
	
	/**
	 * @return the fromTable
	 */
	public TableModel getFromTable() {
		return fromTable;
	}


	/**
	 * @return the toTable
	 */
	public TableModel getToTable() {
		return toTable;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the nullable
	 */
	public boolean isNullable() {
		return nullable;
	}

	public void setBackrefName(String backrefName) {
		this.backrefName = backrefName;
	}
	
	public String getBackrefName() {
		return this.backrefName;
	}
	
	public void setRelationType(RELATION_TYPE relationType) { 
			this.relationType = relationType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fromTable == null) ? 0 : fromTable.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((toTable == null) ? 0 : toTable.hashCode());
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
		Relationship other = (Relationship) obj;
		if (fromTable == null) {
			if (other.fromTable != null)
				return false;
		} else if (!fromTable.equals(other.fromTable))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (toTable == null) {
			if (other.toTable != null)
				return false;
		} else if (!toTable.equals(other.toTable))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Relationship [name=" + name + ", fromTable=" + fromTable + ", toTable=" + toTable + "]";
	}
	
	
}
