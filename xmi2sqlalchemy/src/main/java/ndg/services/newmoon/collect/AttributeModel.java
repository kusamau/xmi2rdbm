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

import ndg.services.newmoon.collect.CollectClassesClosure.CollectionType;


/**
 * Represents a {@link ClassModel} attribute.
 * Temporarily it use a pair associatedClass/associatedDataType
 * to define itself. In future a cleaner solution would be to 
 * bind the associatedClass with a class implementing the specified UMLDataType  
 * 
 * @author mnagni
 *
 */
public class AttributeModel {	
	private ClassModel associatedType;
	private final String name;	
	private final Integer lowerBound;
	private final Integer upperBound;
	private final int lenght;
	private final boolean voidable;
	private final CollectionType collectionType;
	
	/**
	 * @param associatedType
	 * @param name
	 * @param lowerBound
	 * @param upperBound
	 * @param lenght
	 */
	public AttributeModel(ClassModel associatedType, 
			String name, int lowerBound, Integer upperBound, 
			Integer lenght, boolean voidable, CollectionType collectionType) {
		this.associatedType = associatedType;		
		this.name = name;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.lenght = lenght;
		this.voidable = voidable;
		this.collectionType = collectionType;
	}
	
	public AttributeModel(AttributeModel am) {
		this.associatedType = am.getAssociatedType();		
		this.name = am.getName();
		this.lowerBound = am.getLowerBound();
		this.upperBound = am.getUpperBound();
		this.lenght = am.getLenght();
		this.voidable = am.isVoidable();
		this.collectionType = am.getCollectionType();
	}
	
	/**
	 * Returns the associated type.
	 * @return the associatedType
	 */
	public ClassModel getAssociatedType() {	 
		return associatedType;							
	}	
	
	/**
	 * @param associatedType the associatedType to set
	 */
	public void setAssociatedType(ClassModel associatedType) {
		this.associatedType = associatedType;
	}

	public boolean hasMultiplicity() {
		if (upperBound == null || collectionType == null) {
			return false;
		}
			
		return upperBound > 1 				
				|| collectionType.equals(CollectionType.SET)
				|| collectionType.equals(CollectionType.SEQUENCE);
	}
	
	public boolean isNullable() {
		return this.getLowerBound().equals("0") || this.isVoidable();
	}
	
	/**
	 * @return the voidable
	 */
	public boolean isVoidable() {
		return voidable;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the lowerBound
	 */
	public Integer getLowerBound() {
		return lowerBound;
	}
	/**
	 * @return the upperBound
	 */
	public Integer getUpperBound() {
		return upperBound;
	}
	
	/**
	 * @return the collection
	 */
	public CollectionType getCollectionType() {
		return collectionType;
	}
	
	/**
	 * @return the lenght
	 */
	public int getLenght() {
		return lenght;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((associatedType == null) ? 0 : associatedType.hashCode());
		result = prime * result + lenght;
		result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((upperBound == null) ? 0 : upperBound.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
			
		if (obj == null) {
			return false;
		}
			
		if (!AttributeModel.class.isInstance(obj)) {
			return false;
		}
			
		AttributeModel other = (AttributeModel) obj;
		if (associatedType == null) {
			if (other.associatedType != null) {
				return false;
			}				
		} else if (!associatedType.equals(other.associatedType)) {
			return false;
		}
			
		if (lenght != other.lenght) {
			return false;
		}
			
		if (lowerBound == null) {
			if (other.lowerBound != null) {
				return false;
			}				
		} else if (!lowerBound.equals(other.lowerBound)) {
			return false;
		}
			
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (upperBound == null) {
			if (other.upperBound != null) {
				return false;
			}				
		} else if (!upperBound.equals(other.upperBound)) {
			return false;
		}			
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AttributeModel [name=" + name + ", associatedType=" + associatedType + "]";
	}	
}
