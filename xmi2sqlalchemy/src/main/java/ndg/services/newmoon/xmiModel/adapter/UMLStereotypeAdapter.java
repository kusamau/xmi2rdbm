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
package ndg.services.newmoon.xmiModel.adapter;

import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UML_ID;

/**
 * @author mnagni
 *
 */
public class UMLStereotypeAdapter extends UML_IDAdapder implements UMLStereotype {
	
	private UML_ID subtype;
	private UML_ID supertype;
	
	/**
	 * @param id
	 */
	protected UMLStereotypeAdapter(UML_ID id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLGeneralization#getSubtype()
	 */
	public UML_ID getSubtype() {
		return subtype;
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLGeneralization#getsuperType()
	 */
	public UML_ID getsuperType() {
		return supertype;
	}

	/**
	 * @return the supertype
	 */
	public UML_ID getSupertype() {
		return supertype;
	}

	/**
	 * @param supertype the supertype to set
	 */
	protected void setSupertype(UML_ID supertype) {
		this.supertype = supertype;
	}

	/**
	 * @param subtype the subtype to set
	 */
	protected void setSubtype(UML_ID subtype) {
		this.subtype = subtype;
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.adapter.UML_IDAdapder#hashCode()
	 */
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.adapter.UML_IDAdapder#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
}
