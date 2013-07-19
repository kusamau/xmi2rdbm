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
package ndg.services.newmoon.collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ndg.services.newmoon.NewmoonException;
import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.collect.ClassModel.STEREOTYPE;
import ndg.services.newmoon.velocity.python.support.SimpleKeyValue;
import ndg.services.newmoon.xmiModel.UMLAssociationEnd;
import ndg.services.newmoon.xmiModel.UMLAttribute;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLClassDataType;
import ndg.services.newmoon.xmiModel.UMLDataType;
import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UMLValueTagged;
import ndg.services.newmoon.xmiModel.UML_ID;
import ndg.services.newmoon.xmiModel.v11.Dictionary;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * @author mnagni
 *
 */
public class CollectClassesClosure implements Closure {
	public enum CollectionType {NONE, SET, SEQUENCE, DICTIONARY, TRANSFINITE_SET}
	
	private final List<ClassModel> models = new ArrayList<ClassModel>();
	private List<NewmoonException> exceptions = new ArrayList<NewmoonException>();

	private final UMLElementsArchive umlArchive;
	
	public CollectClassesClosure(UMLElementsArchive umlArchive) {
		super();
		this.umlArchive = umlArchive;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
	 */
	@Override
	public void execute(Object input) {
		try {
			processUMLclass((UMLClass) input);
		} catch (NewmoonException e) {
			exceptions.add(e);
		} catch (IOException e) {
			exceptions.add(new NewmoonException(e));
		}
	}

	/**
	 * @return the models
	 */
	List<ClassModel> getModels() {
		return models;
	}

	private STEREOTYPE getStereotypeType(UMLStereotype stereotype) {
		if (stereotype == null)
			return null;

		if (stereotype.getName().equalsIgnoreCase(STEREOTYPE.Enumeration.name()))
			return STEREOTYPE.Enumeration;

		if (stereotype.getName().equalsIgnoreCase(STEREOTYPE.CodeList.name()))
			return STEREOTYPE.CodeList;

		if (stereotype.getName().equalsIgnoreCase(STEREOTYPE.DataType.name()))
			return STEREOTYPE.DataType;

		if (stereotype.getName().equalsIgnoreCase(STEREOTYPE.Union.name()))
			return STEREOTYPE.Union;

		if (stereotype.getName().equalsIgnoreCase(STEREOTYPE.Metaclass.name()))
			return STEREOTYPE.Metaclass;
		
		if (stereotype.getName().equalsIgnoreCase(STEREOTYPE.Type.name()))
			return STEREOTYPE.Type;
		return STEREOTYPE.None;
	}

	private ClassModel processUMLclass(UMLClass umlClass) throws NewmoonException, IOException {
		ClassModel cm = getExternalModel(umlClass);
		if (cm == null && umlClass != null) {
			cm = new ClassModel(umlClass);
			models.add(cm);
			collectData(cm);
		}
		return cm;
	}

	private void collectData(ClassModel cm) throws NewmoonException, IOException {
		processStereotype(cm);
		collectGeneralization(cm);
		collectAttributes(cm);
	}

	private void collectAttributes(ClassModel cm) throws NewmoonException, IOException {
		CollectAttributesClosure cac = new CollectAttributesClosure(cm);
		CollectionUtils.forAllDo(cm.getAssociatedClass().getAttributes(), cac);
		CollectionUtils.forAllDo(cm.getAssociatedClass().getAssociationEnds(), cac);

		// Temporary commented
		/*
		 * if (!CollectionUtils.isEmpty(cac.getExceptions())) throw
		 * cac.getExceptions().get(0);
		 */
		CollectionUtils.addAll(cm.getAttributeModel(), cac.getRet().iterator());
	}

	/**
	 * Generates a <code>Map</code> which the template will translate in a
	 * format like
	 * <p>
	 * from $key import $value
	 * </p>
	 * 
	 * @param umlClass
	 *            the UMLClass to analyze
	 * @param vcontext
	 *            a VelocityContext instance
	 * @param importCollector
	 *            the instance in charge to collect all the import
	 *            statements
	 * @throws IOException
	 * @throws NewmoonException
	 * */
	private void collectGeneralization(ClassModel cm) throws NewmoonException, IOException {
		Set<UMLClass> supertypes = umlArchive.getSupertypesForUMLClass(
				cm.getAssociatedClass());
		Iterator<UMLClass> items = supertypes.iterator();

		UMLClass item = null;
		while (items.hasNext()) {
			item = items.next();			
			CollectionUtils.addIgnoreNull(cm.getParents(), processUMLclass(item));
		}
	}


	
	private void processStereotype(ClassModel cm) {
		// Set<UMLStereotype> stereotypes =
		// getImplementations(cm.getAssociatedClass().getUmlStereotypes());
		Set<UMLStereotype> stereotypes = cm.getAssociatedClass().getUmlStereotypes();

		// For now I assume a single stereotype
		if (CollectionUtils.isEmpty(stereotypes))
			return;

		UMLStereotype stereotype = (UMLStereotype) CollectionUtils.get(stereotypes.iterator(), 0);

		cm.setStereotype(getStereotypeType(stereotype));
	}

	private ClassModel getExternalModel(UMLClass umlClass) throws NewmoonException, IOException {
		ClassModel tm = new ClassModel(umlClass);
		synchronized (models) {
			if (models.contains(tm)) {
				for (ClassModel model : models) {
					if (model.equals(tm))
						return model;
				}
			}
		}
		return null;
	}

	private class CollectAttributesClosure implements Closure {
		private final Set<AttributeModel> ret = new HashSet<AttributeModel>();
		private final ClassModel cm;
		private final List<NewmoonException> exceptions = new ArrayList<NewmoonException>();

		/**
		 * @param cm
		 */
		public CollectAttributesClosure(ClassModel cm) {
			super();
			this.cm = cm;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.collections.Closure#execute(java.lang.Object)
		 */
		@Override
		public void execute(Object input) {
			try {
				if (input instanceof UMLAttribute) {
					ret.add(extractAttribute((UMLAttribute) input));
				} else if (input instanceof UMLAssociationEnd)
					ret.add(extractAttribute((UMLAssociationEnd) input));
			} catch (NewmoonException e) {
				this.exceptions.add(e);
			} catch (IOException e) {
				this.exceptions.add(new NewmoonException(e));
			}
		}

		private void generateSimpleKeyValues(UMLAttribute umlId) {				
			switch (cm.getStereotype()) {
			case Enumeration:
			case CodeList:
					cm.getSimpleKeyValues().add(new SimpleKeyValue(umlId.getName(), umlId.getName()));
			}
		}

		/**
		 * @return the ret
		 */
		Set<AttributeModel> getRet() {
			return ret;
		}

		private AttributeModel extractAttribute(UMLAttribute umlAttribute) throws NewmoonException,
				IOException {
			generateSimpleKeyValues(umlAttribute);
			return extractAttribute(umlAttribute.getClassifier(), umlAttribute, cm,
					hasMolteplicity(umlAttribute), umlArchive.isVoidable(umlAttribute));
		}

		private AttributeModel extractAttribute(UMLAssociationEnd end) throws NewmoonException, IOException {
			return extractAttribute(end.getType(), end, cm, hasMolteplicity(end), false);
		}

		private AttributeModel extractAttribute(UML_ID type, UMLValueTagged valueTagged,
				ClassModel attributeOwner, boolean hasMultiplicity, boolean isVoidable) throws NewmoonException,
				IOException {
			UMLClassDataType umlId = umlArchive.getType(type);
			if (umlId == null)
				throw new NewmoonException("No type for UML_ID: " + type);
			return processAttribute(umlId, valueTagged, hasMultiplicity, isVoidable);
		}

		private <T extends UMLClassDataType> AttributeModel processAttribute(T umlAttribute, UMLValueTagged valueTagged,
				boolean hasMultiplicity, boolean isVoidable) throws NewmoonException, IOException {

			if (!(umlAttribute instanceof UMLClass) 
					&& !(umlAttribute instanceof UMLDataType))
				throw new NewmoonException("No type for UML_ID: " + umlAttribute);

			ClassModel attrClassModel = null;
			//Processes the attribute type (the called method will skip it if already exists)
			if (umlAttribute instanceof UMLClass)
				attrClassModel = processUMLclass((UMLClass) umlAttribute);

			if (umlAttribute instanceof UMLDataType) {
				UMLClass attrClass = extractUMLClassByDataType((UMLDataType)umlAttribute);
				//Does the Setting missed in the NmParser ingestion
				((UMLDataType)umlAttribute).setType(attrClass);
				attrClassModel = processUMLclass(attrClass);
			}
			
			return new AttributeModel(attrClassModel, 
					valueTagged.getName(), 
					extractIntegerValue(valueTagged, Dictionary.LOWER_BOUND), 
					extractIntegerValue(valueTagged, Dictionary.UPPER_BOUND),
					extractIntegerValue(valueTagged, Dictionary.LENGTH), 
					isVoidable,
					extractCollectionType(umlAttribute.getName()));
		}
		
		private Integer extractIntegerValue(UMLValueTagged valueTagged, String bound) {
			String sBound = null;
			if (valueTagged instanceof UMLAssociationEnd) {
				sBound = extractMulteplicity((UMLAssociationEnd)valueTagged, bound); 
			} else {
				sBound = umlArchive.getTaggedValue(valueTagged, bound);
			} 
			return extractIntegerValue(sBound);
		}
		
		private String extractMulteplicity(UMLAssociationEnd umlAssociationEnd, String bound){
			if (umlAssociationEnd.getMultiplicity().contains("..")) {
				String[] limits = umlAssociationEnd.getMultiplicity().split("\\.\\.");
				if (bound.equals(Dictionary.LOWER_BOUND)) {
					return limits[0];
				} else {
					return limits[1];
				}
			}	
			return null;
		}
		
		private Integer extractIntegerValue(String value) {
			if (value == null)
				return 0;
			if (StringUtils.isNumeric(value))
				return Integer.parseInt(value);
			if (value.equals("*"))
				return Integer.MAX_VALUE;
			return 0;
		}
		
		private <T extends UMLClassDataType> UMLClass extractUMLClassByDataType(T umlDataType) {
			/* For now I assume that any DataType name is of the format 
			 * 'xxx<UMLClass_name>' 
			 * */
			int beginIndex = umlDataType.getName().indexOf("<") + 1;
			int endIndex = umlDataType.getName().indexOf(">");
			String umlClassName = umlDataType.getName();
			if (beginIndex > 0 && endIndex > 0)
				umlClassName = umlDataType.getName().substring(beginIndex, endIndex);
			return umlArchive.getUMLClassByName(umlClassName);
		}

		/**
		 * Verifies if the given parameter has multiplicity greater than 1.
		 * Please note that this is not the Attribute class type
		 * multiplicity, which is calculated on the overall use among the
		 * models which compone this model
		 * 
		 * @param umlAttribute
		 *            the attribute to evaluate
		 * @return <code>true</code> if the multiplicity is greater than 1
		 *         or equal to "*", <code>false</code> otherwise
		 * */
		private boolean hasMolteplicity(UMLAttribute umlAttribute) {
			return hasMolteplicity(
					umlArchive.getTaggedValue(umlAttribute, Dictionary.LOWER_BOUND),
					umlArchive.getTaggedValue(umlAttribute, Dictionary.UPPER_BOUND));
		}

		/**
		 * Verifies if the given parameter has multiplicity greater than 1.
		 * Please note that this is not the Attribute class type
		 * multiplicity, which is calculated on the overall use among the
		 * models which compone this model
		 * 
		 * @param umlAttribute
		 *            the attribute to evaluate
		 * @return <code>true</code> if the multiplicity is greater than 1
		 *         or equal to "*", <code>false</code> otherwise
		 * */
		private boolean hasMolteplicity(UMLAssociationEnd umlAssociationEnd) {
			String lowerBound = null;
			String upperBound = null;
			if (umlAssociationEnd.getMultiplicity().contains("..")) {
				String[] limits = umlAssociationEnd.getMultiplicity().split("\\.\\.");
				lowerBound = limits[0];
				upperBound = limits[1];
			}
			return hasMolteplicity(lowerBound, upperBound);
		}

		private boolean hasMolteplicity(String lowerBound, String upperBound) {
			String lb = StringUtils.defaultIfEmpty(lowerBound, "");
			String ub = StringUtils.defaultIfEmpty(upperBound, "");
			if (NumberUtils.isNumber(lb) && NumberUtils.toInt(lb) > 1) {
				return true;
			}

			if (ub.equals("*") || (NumberUtils.isNumber(ub) && NumberUtils.toInt(ub) > 1)) {
				return true;
			}
			return false;
		}

		/**
		 * Verifies if the given valueTagged element has been defined as
		 * <code>Set<code>. This is done checking
		 * if the element owns a TaggedValue named {@link Dictionary#TYPE} whose value is formatted like
		 * <code>Set<One_Class_Name></code>. This kind of structure is
		 * usually found in classes attribute's whose stereotype is Union
		 * 
		 * @param valueTagged
		 *            the UMLValueTagged element
		 **/
		private boolean isSetType(UMLValueTagged valueTagged) {
			String value = umlArchive.getTaggedValue(valueTagged, Dictionary.TYPE);
			if (value == null)
				return false;
			return (value.startsWith("Set<") || value.startsWith("Set&lt;"));
		}
	}
	
	/**
	 * @see EmbeddedType#EmbeddedType()
	 **/
	private CollectionType extractCollectionType(String definition) {
		if (definition.matches("Sequence(&lt;|<).*(&gt;|>)"))
			return CollectionType.SEQUENCE;
		if (definition.matches("Set(&lt;|<).*(&gt;|>)"))
			return CollectionType.SET	;
		if (definition.matches("Dictionary(&lt;|<).*(&gt;|>)"))
			return CollectionType.DICTIONARY	;		
		if (definition.matches("TranfiniteSet(&lt;|<).*(&gt;|>)"))
			return CollectionType.DICTIONARY	;		
		if (definition.matches("(&lt;|<)undefined(&gt;|>)"))
			return CollectionType.NONE;
		else 
			return CollectionType.NONE; 	
	}
}
