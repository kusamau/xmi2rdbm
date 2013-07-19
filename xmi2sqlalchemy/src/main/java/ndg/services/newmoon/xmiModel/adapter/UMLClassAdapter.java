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

import java.util.HashSet;
import java.util.Set;

import ndg.services.newmoon.xmiModel.UMLAssociationEnd;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLGeneralization;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UML_ID;

/**
 * @author mnagni
 *
 */
public class UMLClassAdapter extends UMLClassDataTypeAdapter implements UMLClass {

	private final Set<UMLGeneralization> generalization = new HashSet<UMLGeneralization>();
	private final Set<UMLAssociationEnd> associationEnds = new HashSet<UMLAssociationEnd>();
	private final Set<UMLStereotype> umlStereotypes = new HashSet<UMLStereotype>();

	/**
	 * @param id
	 * @param name
	 * @param root
	 * @param leaf
	 * @param abstrakt
	 */
	public UMLClassAdapter(UML_ID id, boolean root, boolean leaf, boolean abstrakt, UMLModel umlPackage) {
		super(id, root, leaf, abstrakt, umlPackage);
		if (getUmlPackage() != null)
			getUmlPackage().getUmlClasses().add(this);
	}


	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLClass#getGeneralization()
	 */
	public Set<UMLGeneralization> getGeneralization() {
		return generalization; 
	}
		
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLClass#getUMLStereotype()
	 */
	@Override
	public Set<UMLStereotype> getUmlStereotypes() {
		return umlStereotypes;
	}

	/**
	 * @return the associationEnds
	 */
	public Set<UMLAssociationEnd> getAssociationEnds() {
		return associationEnds;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UMLClassAdapter [name=" + getName() + ", umlPackage=" + getUmlPackage() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
