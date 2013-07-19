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

import ndg.services.newmoon.xmiModel.UMLAttribute;
import ndg.services.newmoon.xmiModel.UMLClassDataType;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLPackage;
import ndg.services.newmoon.xmiModel.UML_ID;

/**
 * @author mnagni
 *
 */
public class UMLClassDataTypeAdapter extends UML_IDAdapder implements UMLClassDataType {

	private boolean root;
	private boolean leaf;
	private boolean abstrakt;
	private final UMLModel umlModel;
	private final Set<UMLAttribute> attributes = new HashSet<UMLAttribute>();

	/**
	 * @param id
	 * @param name
	 * @param root
	 * @param leaf
	 * @param abstrakt
	 */
	public UMLClassDataTypeAdapter(UML_ID id, boolean root, boolean leaf, boolean abstrakt, UMLModel umlModel) {
		super(id);
		this.root = root;
		this.leaf = leaf;
		this.abstrakt = abstrakt;
		this.umlModel = umlModel;
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLClass#isRoot()
	 */
	public boolean isRoot() {
		return this.root;
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLClass#isLeaf()
	 */
	public boolean isLeaf() {
		return this.leaf;
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLClass#isAbstract()
	 */
	public boolean isAbstrakt() {
		return this.abstrakt;
	}

	/**
	 * @return the instance UMLModel
	 */
	public UMLModel getUmlModel() {
		return umlModel;
	}	

	/**
	 * @param root the root to set
	 */
	protected void setRoot(boolean root) {
		this.root = root;
	}

	/**
	 * @param leaf the leaf to set
	 */
	protected void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	/**
	 * @param abstrakt the abstrakt to set
	 */
	protected void setAbstrakt(boolean abstrakt) {
		this.abstrakt = abstrakt;
	}	
	
	/**
	 * @return the attributes
	 */
	public Set<UMLAttribute> getAttributes() {
		return attributes;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UMLClassAdapter [name=" + getName() + ", umlModel=" + umlModel + "]";
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

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UMLClassDataType#getUmlPackage()
	 */
	public UMLModel getUmlPackage() {
		if (umlModel instanceof UMLPackage)
			return umlModel;
		return null;
	}
}