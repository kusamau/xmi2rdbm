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

import ndg.services.newmoon.xmiModel.EAStub;
import ndg.services.newmoon.xmiModel.UML_ID;

/**
 * @author mnagni
 *
 */
public class EAStubAdapter extends UML_IDAdapder implements EAStub {
	private final String umlType;
	private String origialUmlID;
	
	//this is a runtime paramenter more that an element attribute. Could be worth remove it in future
	private boolean underProcessing = false;

	/**
	 * @param id
	 * @param name
	 * @param umlType
	 */
	public EAStubAdapter(UML_ID id, String umlType) {
		super(id);
		this.umlType = umlType;
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.xmiModel.EAStub#getUMLType()
	 */
	public String getUmlType() {
		return umlType;
	}
	
	/**
	 * @return the origialUmlID
	 */
	public String getOrigialUmlID() {
		return origialUmlID;
	}

	/**
	 * @param origialUmlID the origialUmlID to set
	 */
	public void setOrigialUmlID(String origialUmlID) {
		this.origialUmlID = origialUmlID;
	}

	/**
	 * @return the underProcessing
	 */
	public boolean isUnderProcessing() {
		return underProcessing;
	}

	/**
	 * @param underProcessing the underProcessing to set
	 */
	public void setUnderProcessing(boolean underProcessing) {
		this.underProcessing = underProcessing;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EAStubAdapter [name=" + getName() + ", umlType=" + umlType + "]";
	}
}
