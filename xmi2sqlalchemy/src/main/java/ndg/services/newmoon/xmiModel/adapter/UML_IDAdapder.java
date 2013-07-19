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
import java.util.List;

import ndg.services.newmoon.xmiModel.EAStub;
import ndg.services.newmoon.xmiModel.UML_ID;

/**
 * @author mnagni
 *
 */
public class UML_IDAdapder extends UML_ElementAdapter implements UML_ID {

	private final String id;
	private final String modelId;
	private final boolean referenceId;
	private final List<String> keyNames = new ArrayList<String>();

	/**
	 * Creates a new UML_ID instance.
	 * @param id the element id
	 * @param modelId the model to which this element is associated
	 * @param name the element name
	 * @param referenceId define if the instance is a reference	 * 
	 */
	public UML_IDAdapder(String id, String modelId, String name, boolean referenceId) {
		super(name);
		this.id = id;
		this.modelId = modelId;
		this.referenceId = referenceId;
	}
	
	/**
	 * Creates a new UML_ID instance. As {@link #UML_IDAdapder(String, String, String, boolean)} setting 
	 * <code>referenceId</code> = <code>false</code>
	 * @param id the element id
	 * @param modelId the model to which this element is associated
	 * @param name the element name 
	 */
	public UML_IDAdapder(String id, String modelId, String name) {
		super(name);
		this.id = id;
		this.modelId = modelId;
		this.referenceId = false;
	}
	
	/**
	 * Creates a new UML_ID instance.
	 * @param id the element id
	 * @param modelId the model to which this element is associated
	 * @param name the element name
	 * @param referenceId define if the instance is a reference
	 */
	public UML_IDAdapder(UML_ID umlId, boolean referenceId) {
		this(umlId.getId(), umlId.getModelId(), umlId.getName(), referenceId);
	}
	
	/**
	 * Creates a new UML_ID instance. As {@link #UML_IDAdapder(UML_ID, boolean)} setting 
	 * <code>referenceId</code> = <code>false</code>
	 * 
	 * @param id the element id
	 * @param modelId the model to which this element is associated
	 * @param name the element name
	 */	
	public UML_IDAdapder(UML_ID umlId) {
		this(umlId.getId(), umlId.getModelId(), umlId.getName(), false);
	}
	
	/**
	 * Creates a new UML_ID instance. {@link #getName()} is set to <code>null</name> by default
	 * @param id
	 * @param modelId
	 */
	public UML_IDAdapder(String id, String modelId) {
		this(id, modelId, null);
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the modelId
	 */
	public String getModelId() {
		return modelId;
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.UML_ID#isReferenceId()
	 */
	@Override
	public boolean isReferenceId() {
		return referenceId;
	}

	/**
	 * @return the keyNames
	 */
	public List<String> getKeyNames() {
		return keyNames;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((modelId == null) ? 0 : modelId.hashCode());
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
		if (!UML_ID.class.isAssignableFrom(obj.getClass()))
			return false;
		UML_IDAdapder other = (UML_IDAdapder) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (modelId == null) {
			if (other.modelId != null)
				return false;
		} else if (!modelId.equals(other.modelId))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UML_IDAdapder [id=" + getId() + " name= " + getName() + " modelId=" + getModelId() + " ]";
	}
}
