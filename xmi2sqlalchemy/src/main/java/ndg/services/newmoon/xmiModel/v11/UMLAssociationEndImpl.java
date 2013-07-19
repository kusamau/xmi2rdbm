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
package ndg.services.newmoon.xmiModel.v11;

import ndg.services.newmoon.xmiModel.UMLAssociation;
import ndg.services.newmoon.xmiModel.adapter.UMLAssociationEndAdapter;
import ndg.services.newmoon.xmiModel.adapter.UML_IDAdapder;

import org.w3c.dom.Element;

/**
 * @author mnagni
 *
 */
public class UMLAssociationEndImpl extends UMLAssociationEndAdapter {

	/**
	 * @param aggregation
	 * @param type
	 * @param multeplicity
	 * @param name
	 * @param navigable
	 */
	public UMLAssociationEndImpl(Element el, String modelId, UMLAssociation parent) {
		super(el.getAttribute(Dictionary.AGGREGATION), 
				new UML_IDAdapder(el.getAttribute(Dictionary.TYPE), modelId), 
				el.getAttribute(Dictionary.MULTIPLICITY),
				el.getAttribute(Dictionary.NAME),
				Boolean.parseBoolean(el.getAttribute(Dictionary.NAVIGABLE)), parent);
	}
}