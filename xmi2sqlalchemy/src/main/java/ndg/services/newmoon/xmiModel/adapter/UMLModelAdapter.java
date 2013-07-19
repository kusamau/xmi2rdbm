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

import ndg.services.newmoon.NmParserHelper;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLDataType;
import ndg.services.newmoon.xmiModel.UMLDependency;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLPackage;
import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UML_ID;

/**
 * @author mnagni
 *
 */
public class UMLModelAdapter extends UML_IDAdapder implements UMLModel {

	private final List<UMLClass> umlClasses = new ArrayList<UMLClass>();
	private final UMLModel parent;
	private final Set<UMLStereotype> umlStereotypes = new HashSet<UMLStereotype>();
	private final List<UMLDependency> umlDependencies = new ArrayList<UMLDependency>();
	private final List<UMLPackage> umlPackages = new ArrayList<UMLPackage>();
	private final List<UMLDataType> umlDataType = new ArrayList<UMLDataType>();
	
	/**
	 * @param id
	 * @param name
	 */
	public UMLModelAdapter(UML_ID id, UMLModel parent) {
		super(id);
		this.parent = parent;
	}

	/**
	 * @return the umlClasses
	 */
	public List<UMLClass> getUmlClasses() {
		return umlClasses;
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLPackage#getParent()
	 */
	public UMLModel getParent() {
		return parent;
	}	
	
	/**
	 * @return the umlPackages
	 */
	public List<UMLPackage> getUmlPackages() {
		return umlPackages;
	}
	
	/**
	 * @return the umlDependencies
	 */
	public List<UMLDependency> getUmlDependencies() {
		return umlDependencies;
	}
	
	/**
	 * @return the umlDataType
	 */
	public List<UMLDataType> getUmlDataType() {
		return umlDataType;
	}
	
	/**
	 * @return the umlStereotypes
	 */
	public Set<UMLStereotype> getUmlStereotypes() {
		return umlStereotypes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return NmParserHelper.getPackageParentName(this);
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
