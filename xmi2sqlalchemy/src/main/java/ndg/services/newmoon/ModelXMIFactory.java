/**
 * BSD Licence Copyright (c) 2009, Science & Technology Facilities Council
 * (STFC) All rights reserved. Redistribution and use in source and binary
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of the Science & Technology
 * Facilities Council (STFC) nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package ndg.services.newmoon;

import ndg.services.newmoon.xmiModel.EAStub;
import ndg.services.newmoon.xmiModel.UMLAssociation;
import ndg.services.newmoon.xmiModel.UMLAssociationEnd;
import ndg.services.newmoon.xmiModel.UMLAttribute;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLClassDataType;
import ndg.services.newmoon.xmiModel.UMLClassifierRole;
import ndg.services.newmoon.xmiModel.UMLCollaboration;
import ndg.services.newmoon.xmiModel.UMLDataType;
import ndg.services.newmoon.xmiModel.UMLDependency;
import ndg.services.newmoon.xmiModel.UMLGeneralization;
import ndg.services.newmoon.xmiModel.UMLInterface;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLPackage;
import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UMLStereotyped;
import ndg.services.newmoon.xmiModel.UMLTaggedValue;

import org.w3c.dom.Element;

/**
 * @author mnagni
 *
 */
public interface ModelXMIFactory {

	public abstract EAStub createEAStub(Element el, String modelId);

	public abstract UMLModel createUMLModel(Element el, String modelId, UMLModel parent);

	public abstract UMLClassifierRole createUMLClassifierRole(Element el, String modelId);

	public abstract UMLDataType createUMLDataType(Element el, String modelId, UMLModel umlModel, UMLClassDataType type);

	public abstract UMLCollaboration createUMLCollaboration(Element el, String modelId);

	public abstract UMLDependency createUMLDependency(Element el, String modelId);

	public abstract UMLPackage createUMLPackage(Element el, String modelId, UMLModel parent);

	public abstract UMLGeneralization createUMLGeneralization(Element el, String modelId);

	public abstract UMLAssociation createUMLAssociation(Element el, String modelId);

	public abstract UMLAssociationEnd createUMLAssociationEnd(Element el, String modelId, UMLAssociation parent);

	public abstract UMLClass createUMLClass(Element el, String modelId, UMLModel umlModel);
	
	public abstract UMLInterface createUMLInterface(Element el, String modelId, UMLModel umlModel);

	public abstract UMLAttribute createUMLAttribute(Element el, UMLClass ownerClass);
	
	public abstract UMLStereotype createUMLStereotype(Element el, String modelId, UMLStereotyped stereotypeOwner);
	
	public abstract UMLTaggedValue createUMLTaggedValue(Element el);

}