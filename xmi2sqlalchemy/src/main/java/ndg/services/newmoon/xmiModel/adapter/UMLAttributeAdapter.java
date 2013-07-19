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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ndg.services.newmoon.xmiModel.UMLAttribute;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UMLTaggedValue;
import ndg.services.newmoon.xmiModel.UML_ID;

/**
 * @author mnagni
 *
 */
public class UMLAttributeAdapter extends UML_ElementAdapter implements UMLAttribute {

	private final UMLClass umlClassOwner;
	private UML_ID classifier;
	private final Set<UMLStereotype> umlStereotypes = new HashSet<UMLStereotype>();
	private final List<UMLTaggedValue> umlTaggedValues = new ArrayList<UMLTaggedValue>();
	
	/*
	private String lowerBound;
	private String upperBound;
	private String lenght;
	private String precision;
	*/
	
	/**
	 * @param id
	 * @param umlClassType
	 * @param umlClassOwner
	 * @param name
	 */
	protected UMLAttributeAdapter(String name, UMLClass umlClassOwner) {
		super(name);		
		this.umlClassOwner = umlClassOwner;
		if (this.umlClassOwner != null)
			this.umlClassOwner.getAttributes().add(this);
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLAttribute#getUmlClassOwner()
	 */
	public UMLClass getUmlClassOwner() {
		return umlClassOwner;
	}

	/**
	 * @return the umlStereotype
	 */
	public Set<UMLStereotype> getUmlStereotypes() {
		return umlStereotypes;
	}
	
	/**
	 * @return the umlTaggedValue
	 */
	public List<UMLTaggedValue> getUmlTaggedValues() {
		return umlTaggedValues;
	}

	/**
	 * @return the classifier
	 */
	public UML_ID getClassifier() {
		return classifier;
	}

	/**
	 * @param classifier the classifier to set
	 */
	public void setClassifier(UML_ID classifier) {
		this.classifier = classifier;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UMLAttributeAdapter [name=" + getName() 
				+ ", classifier=" + classifier 
				+ ", umlClassOwner=" + umlClassOwner + "]";
	}
}
