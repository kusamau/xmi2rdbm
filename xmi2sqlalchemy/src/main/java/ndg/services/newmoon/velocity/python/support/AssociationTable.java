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
 * 
 * Represent a third table in the many-to-many relation moddel 
 * @author mnagni
 *
 */
public class AssociationTable {
	private final TableModel rightTable;
	private final TableModel leftTable;
	/**
	 * @param rightTable
	 * @param leftTable
	 */
	public AssociationTable(TableModel rightTable, TableModel leftTable) {
		super();
		this.rightTable = rightTable;
		this.leftTable = leftTable;
	}
	
	/**
	 * @param rightTable
	 * @param leftTable
	 */
	public AssociationTable(Relationship relationship) {
		super();
		this.rightTable = relationship.getToTable();
		this.leftTable = relationship.getFromTable();
	}
	
	/**
	 * @return the rightTable
	 */
	public TableModel getRightTable() {
		return rightTable;
	}
	/**
	 * @return the leftModel
	 */
	public TableModel getLeftTable() {
		return leftTable;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftTable == null) ? 0 : leftTable.hashCode());
		result = prime * result + ((rightTable == null) ? 0 : rightTable.hashCode());
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
		AssociationTable other = (AssociationTable) obj;
		if (leftTable == null) {
			if (other.leftTable != null)
				return false;
		} else if (!leftTable.equals(other.leftTable))
			return false;
		if (rightTable == null) {
			if (other.rightTable != null)
				return false;
		} else if (!rightTable.equals(other.rightTable))
			return false;
		return true;
	}
}
