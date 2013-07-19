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
import ndg.services.newmoon.xmiModel.v11.EAStubImpl;
import ndg.services.newmoon.xmiModel.v11.UMLAssociationEndImpl;
import ndg.services.newmoon.xmiModel.v11.UMLAssociationImpl;
import ndg.services.newmoon.xmiModel.v11.UMLAttributeImpl;
import ndg.services.newmoon.xmiModel.v11.UMLClassImpl;
import ndg.services.newmoon.xmiModel.v11.UMLClassifierRoleImpl;
import ndg.services.newmoon.xmiModel.v11.UMLCollaborationImpl;
import ndg.services.newmoon.xmiModel.v11.UMLDataTypeImpl;
import ndg.services.newmoon.xmiModel.v11.UMLDependencyImpl;
import ndg.services.newmoon.xmiModel.v11.UMLGeneralizationImpl;
import ndg.services.newmoon.xmiModel.v11.UMLInterfaceImpl;
import ndg.services.newmoon.xmiModel.v11.UMLModelImpl;
import ndg.services.newmoon.xmiModel.v11.UMLPackageImpl;
import ndg.services.newmoon.xmiModel.v11.UMLStereotypeImpl;
import ndg.services.newmoon.xmiModel.v11.UMLTaggedValueImpl;

import org.w3c.dom.Element;

/**
 * @author mnagni
 *
 */
public class ModelXMI11Factory implements ModelXMIFactory {

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createEAStub(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public EAStub createEAStub(Element el, String modelId){
		return new EAStubImpl(el, modelId);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLModel(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLModel createUMLModel(Element el, String modelId, UMLModel parent){
		return new UMLModelImpl(el, modelId, parent);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLClassifierRole(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLClassifierRole createUMLClassifierRole(Element el, String modelId){
		return new UMLClassifierRoleImpl(el, modelId);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLDataType(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLDataType createUMLDataType(Element el, String modelId, UMLModel umlModel, UMLClassDataType type){
		return new UMLDataTypeImpl(el, modelId, umlModel, type);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLCollaboration(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLCollaboration createUMLCollaboration(Element el, String modelId){
		return new UMLCollaborationImpl(el, modelId);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLDependency(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLDependency createUMLDependency(Element el, String modelId){
		return new UMLDependencyImpl(el, modelId);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLPackage(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLPackage createUMLPackage(Element el, String modelId, UMLModel parent){
		return new UMLPackageImpl(el, modelId, parent);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLGeneralization(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLGeneralization createUMLGeneralization(Element el, String modelId){
		return new UMLGeneralizationImpl(el, modelId);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLAssociation(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLAssociation createUMLAssociation(Element el, String modelId){
		return new UMLAssociationImpl(el, modelId);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLAssociationEnd(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLAssociationEnd createUMLAssociationEnd(Element el, String modelId, UMLAssociation parent){
		return new UMLAssociationEndImpl(el, modelId, parent);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLClass(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLClass createUMLClass(Element el, String modelId, UMLModel ownerPackage){
		return new UMLClassImpl(el, modelId, ownerPackage);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLInterface(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLInterface createUMLInterface(Element el, String modelId, UMLModel ownerPackage){
		return new UMLInterfaceImpl(el, modelId, ownerPackage);
	}	
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLAttribute(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLAttribute createUMLAttribute(Element el, UMLClass ownerClass){
		return new UMLAttributeImpl(el, ownerClass);
	}
	
	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLAttribute(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public UMLStereotype createUMLStereotype(Element el, String modelId, UMLStereotyped owner){
		return new UMLStereotypeImpl(el, modelId, owner);
	}

	/* (non-Javadoc)
	 * @see ndg.services.newmoon.ModelXMIFactory#createUMLTaggedValue(org.w3c.dom.Element)
	 */
	@Override
	public UMLTaggedValue createUMLTaggedValue(Element el) {
		return new UMLTaggedValueImpl(el);
	}	
}
