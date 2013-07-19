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
package ndg.services.newmoon.xmiModel;

import java.util.List;


/**
 * @author mnagni
 *
 */
public interface UML_ID extends UML_Element {
	/**
	 * Identifies uniquely an element inside an xmi model.
	 * Cannot be <code>null</code>
	 * @return the element <code>xmi.id</code>
	 **/
	String getId();
	
	/**
	 * Allows to discriminate between different xmi models.
	 * Should be the same for all the elements contained in one xmi model
	 * Cannot be <code>null</code> 
	 * @return the xmi model <code>xmi.id</code> 
	 **/
	String getModelId(); //getXMIDocId();
	
	/**
	 * Allows to define this instance as "Reference" instead as a real UML_ID.
	 * @return <code>true</code> if the instance is a reference 
	 **/
	boolean isReferenceId();
	
	/**
	 * Returns the keyName by which this model has been generated.
	 * In other words a model importing other external models has an element
	 * {@link EAStub} whose name is the key to a mapping document
	 * by which is possible to retrieve the document to import. 
	 * @return the key name
	 **/
	List<String> getKeyNames();
}
