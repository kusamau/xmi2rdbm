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
public class ForeignKey {
	private final TableModel tableModel;
	private final String parentAttributeName;
	private boolean useAlter = false;
	private Relationship associatedRelationship = null;

	/**
	 * @param tableModel
	 */
	public ForeignKey(TableModel tableModel, String parentAttributeName) {
		super();
		this.tableModel = tableModel;
		this.parentAttributeName = parentAttributeName;
	}		
	
	/**
	 * @return the associatedRelationship
	 */
	public Relationship getAssociatedRelationship() {
		return associatedRelationship;
	}

	/**
	 * @param associatedRelationship the associatedRelationship to set
	 */
	public void setAssociatedRelationship(Relationship associatedRelationship) {		
		this.associatedRelationship = associatedRelationship;
	}

	/**
	 * @return the parentAttributeName
	 */
	public String getParentAttributeName() {
		return parentAttributeName;
	}

	/**
	 * @return the tableModel
	 */
	public TableModel getTableModel() {
		return tableModel;
	}
	
	/**
	 * @return the useAlter
	 */
	public boolean isUseAlter() {
		return useAlter;
	}

	/**
	 * @param useAlter the useAlter to set
	 */
	public void setUseAlter(boolean useAlter) {
		this.useAlter = useAlter;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parentAttributeName == null) ? 0 : parentAttributeName.hashCode());
		result = prime * result + ((tableModel == null) ? 0 : tableModel.hashCode());
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
		ForeignKey other = (ForeignKey) obj;
		if (parentAttributeName == null) {
			if (other.parentAttributeName != null)
				return false;
		} else if (!parentAttributeName.equals(other.parentAttributeName))
			return false;
		if (tableModel == null) {
			if (other.tableModel != null)
				return false;
		} else if (!tableModel.equals(other.tableModel))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ForeignKey [parentAttributeName=" + parentAttributeName + ", tableModel=" + tableModel + "]";
	}		
	
	
}
