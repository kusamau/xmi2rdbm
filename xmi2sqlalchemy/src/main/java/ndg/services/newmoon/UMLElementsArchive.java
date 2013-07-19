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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.xmiModel.EAStub;
import ndg.services.newmoon.xmiModel.UMLAssociationEnd;
import ndg.services.newmoon.xmiModel.UMLAttribute;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLClassDataType;
import ndg.services.newmoon.xmiModel.UMLDataType;
import ndg.services.newmoon.xmiModel.UMLDependency;
import ndg.services.newmoon.xmiModel.UMLGeneralization;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLPackage;
import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UMLValueTagged;
import ndg.services.newmoon.xmiModel.UML_Element;
import ndg.services.newmoon.xmiModel.UML_ID;
import ndg.services.newmoon.xmiModel.adapter.UMLTaggedValueAdapter;
import ndg.services.newmoon.xmiModel.v11.Dictionary;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 * 
 */
public class UMLElementsArchive {

	private final static int POOL_SIZE = 10;

	private final Set<UML_ID> umlElements = new HashSet<UML_ID>();
	//umlElements just copy its content to this in order to allow an indexed access 
	//to the underlying elements
	private final List<UML_ID> umlElementsList = new ArrayList<UML_ID>();

	/** Used to store the names of the package already managed */
	private final Set<String> pakkages = Collections.synchronizedSet(new HashSet<String>());
	private final Set<String> missingPackages = Collections.synchronizedSet(new LinkedHashSet<String>());

	// Just for lookup
	private final Map<String, UMLModel> modelIdToModel = Collections.synchronizedMap(new HashMap<String, UMLModel>());
	private final Map<EAStub, UMLModel> stubToModel = Collections.synchronizedMap(new HashMap<EAStub, UMLModel>());
//	private final Map<String, UMLClass> classFromNameAndModelID = Collections.synchronizedMap(new HashMap<String, UMLClass>());
//	private final List<UMLAssociation> associations = Collections.synchronizedList(new ArrayList<UMLAssociation>());
	private final Map<UMLModel, List<UMLDependency>> umlDependenciesByUMLModel = new HashMap<UMLModel, List<UMLDependency>>();

	private final List<EAStub> eaStubList = new ArrayList<EAStub>();
	private final List<UMLClass> umlClassList = new ArrayList<UMLClass>();
    private final List<UMLModel> umlModelList = new ArrayList<UMLModel>();
    
    private final Set<ClassModel> modelClasses = new HashSet<ClassModel>();

	private final Map<String, Boolean> multeplicity = new HashMap<String, Boolean>();

	private final Logger logger = LoggerFactory.getLogger(UMLElementsArchive.class);

	private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
	private final List<Future<?>> results = new ArrayList<Future<?>>();

	public enum PropertyMap {
		XMI_MAP("xmiMaps.properties"), XMI_TO_SQL_MAP("xmiToSQLMap.properties");

		private final String propName;

		PropertyMap(String propName) {
			this.propName = propName;
		}

		public String getPropName() {
			return propName;
		}
	};

	/**
	 * Adds an element to the collection unless the element is null.
	 * */
	public boolean appendElement(UML_ID element) {
		synchronized (umlElements) {
			if (CollectionUtils.addIgnoreNull(umlElements, element)) {
				synchronized (umlElementsList) {
					umlElementsList.add(element);
				}
				updateLookup(element);
				return true;
			}				
			return false;
		}
	}
	
	private void updateLookup(UML_ID element) {
		if (element instanceof EAStub) {
			synchronized (eaStubList) {
				CollectionUtils.addIgnoreNull(eaStubList, element);	
			}	
		} else if (element instanceof UMLClass) {
			synchronized (umlClassList) {
				CollectionUtils.addIgnoreNull(umlClassList, element);	
			}	
		} else if (element instanceof UMLModel) {
			synchronized (umlModelList) {
				CollectionUtils.addIgnoreNull(umlModelList, element);	
			}	
		}						
	}
	
	
	
	/**
	 * @return the pakkage
	 */
	public Set<String> getPakkages() {
		return pakkages;
	}

	/**
	 * @return the missingPackages
	 */
	public Set<String> getMissingPackages() {
		return missingPackages;
	}

	/**
	 * Extracts an {@link UML_Element}s using its UML_ID as discriminator. If no
	 * element is found which equals both {@link UML_ID#getId()} and
	 * {@link UML_ID#getModelId()} the algorithm looks for an element satisfing
	 * at least {@link UML_ID#getId()}. Eventually return <code>null</code>
	 * 
	 * @param umlID
	 *            the element id
	 * @return the extracted element, otherwise <code>null</code>
	 * @throws NewmoonException
	 * */
	private UMLClass getUMLClassByOwner(UML_ID elementOwner) {
		if (elementOwner != null && UMLClass.class.isAssignableFrom(elementOwner.getClass())) 
			return (UMLClass)elementOwner;
		
		UMLModel requiringUmlModel = getUMLModelByUML_IDModelId(elementOwner);
		List<UMLDependency> dependencies = getUMLDependenciesByUMLModel(requiringUmlModel);
		
		if (dependencies == null)
			return null;

		// for each dependency, retieve the EAStub then the associated UMLModel
		// then scans it looking
		// for an UMLElement with the given elementName
		for (UMLDependency dependency : dependencies) {
			// finds the UMLModel
			UMLModel requiredUmlModel = getUmlModelByUMLDependency(dependency);
			if (requiredUmlModel == null)
				continue; // should throw an exception (?)

//			UMLClass ret = getUMLClassFromUMLModelByNameAndModelId(elementOwner.getName(), requiredUmlModel.getModelId());
			
			List<UMLClass> classes = getUMLClassesByUMLModel(requiredUmlModel);
			for (UMLClass clazz : classes) {
				if (clazz.getName().equals(elementOwner.getName())) {
					return clazz;
				}					
			}

//			if (ret != null)
//				return ret;
		}
		return null;
	}

	public UMLClassDataType getType(UML_ID umlID) { 
		UML_ID validatedUmlId = getUMLIDByUML_ID(umlID);
		if (validatedUmlId instanceof UMLDataType)
			return (UMLDataType)validatedUmlId;
		else 
			return getUMLClassByOwner(validatedUmlId);
	}
	
	public UMLClass getTypeDefinition(UML_ID umlID) {		
		UML_ID validatedUmlId = getUMLIDByUML_ID(umlID);
		return getUMLClassByOwner(validatedUmlId);
	}	
	
	public UMLModel getUMLModelDefinition(UML_ID umlID) {
		if (umlID instanceof UMLModel) {
			for (UMLModel model : umlModelList){
				if (model.getId().equals(umlID.getId()))
						return model;
			}
		}
		
		UML_ID validatedUmlId = getUMLIDByUML_ID(umlID);
		return getUMLModelByUML_IDModelId(validatedUmlId);
	}
	
	public Set<UMLClass> getSupertypesForUMLClass(UMLClass umlClass) {
		Set<UMLClass> supertypes = new HashSet<UMLClass>();
		
		if (umlClass == null)
			return supertypes;
		
		for(UMLGeneralization generalization : umlClass.getGeneralization()) {
			CollectionUtils.addIgnoreNull(supertypes, getType(generalization.getSupertype()));
		}
		return supertypes;
	}		

	public List<UMLClass> getUMLClassesByUMLModel(UMLModel umlModel) {
		if (umlModel == null)
			return null;
		List<UMLClass> classes = new ArrayList<UMLClass>();
		synchronized (umlElements) {
			CollectionUtils
					.select(umlElements, new UMLClassesByUMLModelPredicate(umlModel), classes);
		}		
		return classes;
	}	
	
	/*
	private Set<UMLGeneralization> getUMLGeneralizationByUMLClass(UMLClass umlClass) {
		synchronized (umlGeneralizationByUMLClass) {
			if (umlGeneralizationByUMLClass.containsKey(umlClass)) {
				return umlGeneralizationByUMLClass.get(umlClass);
			}			
		}		

		Set<UMLGeneralization> generalization = new HashSet<UMLGeneralization>();
		if (umlClass == null)
			return generalization;

		synchronized (umlElements) {
			CollectionUtils
					.select(umlElements, new UMLGeneralizationByUMLClassPredicate(umlClass), generalization);
		}		
		
		if (!CollectionUtils.isEmpty(generalization)) {
			synchronized (umlGeneralizationByUMLClass) {
				umlGeneralizationByUMLClass.put(umlClass, generalization);
			}
		}
		return generalization;
	}	
	*/
	
	private List<UMLDependency> getUMLDependenciesByUMLModel(UMLModel umlModel) {
		synchronized (umlDependenciesByUMLModel) {
			if (umlDependenciesByUMLModel.containsKey(umlModel)) {
				return umlDependenciesByUMLModel.get(umlModel);
			}			
		}
		
		List<UMLDependency> dependencies = new ArrayList<UMLDependency>();
		if (umlModel == null)
			return dependencies;
		synchronized (umlElements) {
			CollectionUtils.select(umlElements, new UMLDependenciesByUMLModelPredicate(umlModel), dependencies);
		}
		if (!CollectionUtils.isEmpty(dependencies)) {
			synchronized (umlDependenciesByUMLModel) {
				umlDependenciesByUMLModel.put(umlModel, dependencies);
			}
		}
		return dependencies;
	}	
	
	public UMLModel getUmlModelByUMLDependency(UMLDependency dependency) {
		EAStub eaStub = getEAStubByUMLDependency(dependency);
				
		// is an external dependency
		if (eaStub != null)
			return getUMLModelByEAStub(eaStub);

		// else have to be inner reference
		UML_ID ret = getUMLIDByUML_ID(dependency.getSupplierId());
		
		if (ret == null) {
			logger.debug("Dependency: " + dependency + " with stub: " + eaStub + " has no associated element");
			return null;
		}
							
		if (ret instanceof UMLModel)
			return (UMLModel)ret;
		
		logger.debug("Dependency: " + dependency + " is a " + ret);		
		return null;
	}

	/*
	private UMLClass getUMLClassFromUMLModelByNameAndModelId(String elementName, String modelId) {
		if (classFromNameAndModelID.containsKey(elementName + "_" + modelId)) {
			return classFromNameAndModelID.get(elementName + "_" + modelId);
		}
		
		UMLClass umlClass = null;
		synchronized (umlClassList) {
			umlClass = (UMLClass)CollectionUtils.find(umlClassList, new UMLClassFromUMLModelByNameAndModelIdPredicate(elementName, modelId));
		}
		
		return umlClass != null ?
			classFromNameAndModelID.put(elementName + "_" + modelId, umlClass) : umlClass;
	}
	*/

	private UMLModel getUMLModelByUML_IDModelId(UML_ID elementOwner) {
		if (elementOwner == null)
			return null;
		
		if (modelIdToModel.containsKey(elementOwner.getModelId())) {
			return modelIdToModel.get(elementOwner.getModelId());
		}
		
		UMLModel umlModel = null;
		synchronized (umlElements) {
			umlModel = (UMLModel)CollectionUtils.find(umlElements, new UMLModelByUML_IDModelIdPredicate(elementOwner));
		}
		
		if (umlModel != null) {
			modelIdToModel.put(elementOwner.getModelId(), umlModel);			
		}
		return umlModel;
	}

	private UML_ID getUMLIDByUML_ID(UML_ID umlId) {
		synchronized (umlElementsList) {
			int index = umlElementsList.indexOf(umlId);
			if (index != -1) {
				return umlElementsList.get(index); 
			}
			return null;			
		}
	}	
	
	public EAStub getEAStubByUMLDependency(UMLDependency dependency) {
		EAStub modelEAStub = null;
		if(eaStubList.contains(dependency.getSupplierId())) {
			modelEAStub = eaStubList.get(eaStubList.indexOf(dependency.getSupplierId()));
		}
		
		return modelEAStub;
	}

	public UML_ID getInternalUMLElementByUMLDependency(UMLDependency dependency) {
		return getUMLIDByUML_ID(dependency.getSupplierId());
		/*
		synchronized (umlElements) {
			return (UML_ID)CollectionUtils.find(umlElements, new InternalUMLElementByUMLDependencyPredicate(dependency));
		}
		*/
	}

	private UMLModel getUMLModelByEAStub(EAStub eaStub) {
		if (stubToModel.containsKey(eaStub)) {
			return stubToModel.get(eaStub);
		}

		// finds the UMLModel
		UMLModel umlModel = null;
		synchronized (umlModelList) {
			umlModel = (UMLModel)CollectionUtils.find(umlModelList, new UMLModelByEAStubPredicate(eaStub));
		}

		if (umlModel != null) {
			stubToModel.put(eaStub, umlModel);			
		}
		return umlModel;		
	}

	/**
	 * Extracts a <code>List</code> of {@link UML_Element}s using its Class.type
	 * as discriminator.
	 * 
	 * @param type
	 *            the class extending <code>UML_Element</code>
	 * @return the extracted list
	 * */
	public <T extends UML_ID> List<T> getUMLElementByType(Class<T> type) {
		return selectUMLElementByPredicate(new UMLElementByClassPredicate<T>(type));
	}
	
	/*
	private List<UMLAssociation> getUMLAssociations() {
		synchronized (associations) {
			if (CollectionUtils.isEmpty(associations))
				associations.addAll(getUMLElementByType(UMLAssociation.class));			
		}
		return associations;
	}
	*/	
	
	private <T extends UML_ID> List<T> selectUMLElementByPredicate(Predicate predicate) {
		synchronized (umlElements) {
			List<T> ret = new ArrayList<T>();
			CollectionUtils.select(umlElements, predicate, ret);
			return ret;
		}
	}

	/*
	public UML_ID getElementImplementation(UMLStereotype stereotype) {
		if (stereotype.getName().equalsIgnoreCase("enumeration")) {
			return CoreImplementation.getASEnumeration();
		} else if (stereotype.getName().equalsIgnoreCase("Union")) {
			return CoreImplementation.getASUnion();
		} else if (stereotype.getName().equalsIgnoreCase("CodeList")) {
			return CoreImplementation.getASCodeList();
		} else if (stereotype.getName().equalsIgnoreCase("DataType")) {
			return CoreImplementation.getASDataType();
		}
		return null;
	}
	*/

	public UMLClass getUMLClassByName(String className) {
		return getElementByName(className, UMLClass.class);
	} 
	
	public <T extends UML_ID> T getElementByName(String className, Class<T> elementType) {
		if (className != null && className.contains("DQ_Scope")){
			System.out.println(className);
			List<UMLClassDataType> elements = getUMLElementByType(UMLClassDataType.class);
			for (UMLClassDataType element : elements) {
				if (element.getName().equals(className))
					System.out.println(element);

				if (element.getKeyNames().contains(className))
					System.out.println(element);
			}
		}
		List<T> elements = getUMLElementByType(elementType);
		for (T element : elements) {
			if (element.getName().equals(className))
				return element;

			if (element.getKeyNames().contains(className))
				return element;
		}
		if (logger.isWarnEnabled()) {
			if (!(className.equals("undefined") || className.isEmpty())) {
				logger.warn("Cannot find " + elementType.getName() + "<" + className + ">");	
			}
			
		}
		return null;
	}

	private final class UMLElementByClassPredicate<T extends UML_ID> implements Predicate {

		private Class<T> type;

		/**
		 * @param umlID
		 */
		public UMLElementByClassPredicate(Class<T> type) {
			super();
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(Object object) {
			return type.isAssignableFrom(object.getClass());
		}
	}
	
	private class UMLModelByUML_IDModelIdPredicate implements Predicate {

		private UML_ID modelId;

		/**
		 * @param modelId
		 */
		public UMLModelByUML_IDModelIdPredicate(UML_ID modelId) {
			super();
			this.modelId = modelId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(Object object) {
			if (object == null)
				return false;

			if (!(!(object instanceof UMLPackage) && object instanceof UMLModel))
				return false;
			if (modelId.getModelId() == null || ((UML_ID) object).getModelId() == null)
				return false;

			return modelId.getModelId().equals(((UML_ID) object).getModelId());
		}
	}

	private class UMLDependenciesByUMLModelPredicate implements Predicate {

		private UMLModel umlModel;

		/**
		 * @param modelId
		 */
		public UMLDependenciesByUMLModelPredicate(UMLModel umlModel) {
			super();
			this.umlModel = umlModel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(Object object) {
			if (!(object instanceof UMLDependency))
				return false;
			return umlModel.getModelId().equals(((UMLDependency) object).getModelId());
		}
	}

//	private class UMLGeneralizationByUMLClassPredicate implements Predicate {
//
//		private UMLClass umlClass;
//
//		/**
//		 * @param modelId
//		 */
//		public UMLGeneralizationByUMLClassPredicate(UMLClass umlClass) {
//			super();
//			this.umlClass = umlClass;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see
//		 * org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
//		 */
//		public boolean evaluate(Object object) {
//			if (!(object instanceof UMLGeneralization))
//				return false;
//			UMLGeneralization umlGeneralization = ((UMLGeneralization) object);
//			return umlClass.getId().equals(umlGeneralization.getSubtype().getId());
//		}
//	}	
	
	private class UMLClassesByUMLModelPredicate implements Predicate {

		private UMLModel umlModel;

		/**
		 * @param modelId
		 */
		public UMLClassesByUMLModelPredicate(UMLModel umlModel) {
			super();
			this.umlModel = umlModel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(Object object) {
			if (!(object instanceof UMLClass))
				return false;
			return umlModel.getModelId().equals(((UMLClass) object).getModelId());
		}
	}	

	private class UMLModelByEAStubPredicate implements Predicate {

		private final EAStub eaStub;

		/**
		 * @param modelId
		 */
		public UMLModelByEAStubPredicate(EAStub eaStub) {
			super();
			this.eaStub = eaStub;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(Object object) {
			if (object instanceof UMLPackage) {
				return ((UMLPackage) object).getName().equals(eaStub.getName());
			} else {
				return ((UMLModel) object).getKeyNames().contains(eaStub.getName());
			}			
		}
	}

//	private class UMLClassFromUMLModelByNameAndModelIdPredicate implements Predicate {
//
//		private final String className;
//		private final String modelId;
//
//		/**
//		 * @param classToSearch
//		 */
//		public UMLClassFromUMLModelByNameAndModelIdPredicate(String className, String modelId) {
//			super();
//			this.className = className;
//			this.modelId = modelId;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see
//		 * org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
//		 */
//		public boolean evaluate(Object object) {
//			return ((UMLClass) object).getName().equals(className) && ((UMLClass) object).getModelId().equals(modelId);
//		}
//	}

	/*
	public void evaluateMulteplicity(Set<UMLClass> classes) throws InterruptedException {
		for (UMLClass clazz : classes) {
			startNewParser(new CheckMulteplicity(clazz.getAttributes()));
		}
		waitParserToComplete();
	}
	*/

	public synchronized void synchPackageElement(String pkgName, boolean found) {
		if (found) {
			getMissingPackages().remove(pkgName);
			if (getPakkages().add(pkgName)  && logger.isInfoEnabled()) {
				if (logger.isInfoEnabled()) {
					StringBuffer sb = new StringBuffer();
					for (String name : getMissingPackages()) {
						sb.append(name + ", ");					
					}
					logger.info("Found: " + pkgName + " - Still missing: " + sb.toString());	
				} 				
			}
		} else {
			if (getMissingPackages().add(pkgName) && logger.isDebugEnabled()) {
				logger.debug("Missing: " + pkgName + " - Still missing: " + getMissingPackages().size());
			}
		}
	}

	public <T> Future<T> startNewParser(Callable<T> xmiParser) {
		synchronized (results) {
			Future<T> ret = pool.submit(xmiParser);
			results.add(ret);
			return ret;
		}
	}

	private boolean cleanAndCheckParserTasks() {
		synchronized (results) {
			Iterator<Future<?>> futures = results.iterator();
			Future<?> future = null;
			while (futures.hasNext()) {
				future = futures.next();
				if (future.isDone()) {
					futures.remove();
				}
			}
			
			if (logger.isInfoEnabled() && (results.size() % 10) == 0)
				logger.info(Integer.toString(results.size()));

			results.notifyAll();
			return results.size() > 0;

		}
	}

	public void waitParserToComplete() throws InterruptedException {
		synchronized (results) {
			while (cleanAndCheckParserTasks()) {
				results.wait(1000);
			}
			getPakkages().clear();
		}
	}

	/**
	 * Verifies if the given parameter has multiplicity greater than 1. Please
	 * note that this is not the Attribute class type multiplicity, which is
	 * calculated on the overall use among the models which compone this model
	 * 
	 * @param umlAttribute
	 *            the attribute to evaluate
	 * @return <code>true</code> if the multiplicity is greater than 1 or equal
	 *         to "*", <code>false</code> otherwise
	 * */
	public boolean hasMolteplicity(UMLAttribute umlAttribute) {
		return hasMolteplicity(getTaggedValue(umlAttribute, Dictionary.LOWER_BOUND),
				getTaggedValue(umlAttribute, Dictionary.UPPER_BOUND));
	}

	/**
	 * Verifies if the given parameter has multiplicity greater than 1. Please
	 * note that this is not the Attribute class type multiplicity, which is
	 * calculated on the overall use among the models which compone this model
	 * 
	 * @param umlAttribute
	 *            the attribute to evaluate
	 * @return <code>true</code> if the multiplicity is greater than 1 or equal
	 *         to "*", <code>false</code> otherwise
	 * */
	public boolean isVoidable(UMLAttribute umlAttribute) {
		for (UMLStereotype stereotype : umlAttribute.getUmlStereotypes()) {
			if (stereotype.getName().equalsIgnoreCase(Dictionary.VOIDABLE))
				return true;
		}
		return false;
	}	
	
	/**
	 * Verifies if the given parameter has multiplicity greater than 1. Please
	 * note that this is not the Attribute class type multiplicity, which is
	 * calculated on the overall use among the models which compone this model
	 * 
	 * @param umlAttribute
	 *            the attribute to evaluate
	 * @return <code>true</code> if the multiplicity is greater than 1 or equal
	 *         to "*", <code>false</code> otherwise
	 * */
	public boolean hasMolteplicity(UMLAssociationEnd umlAssociationEnd) {
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
	 * @return the multeplicity
	 */
	public Map<String, Boolean> getMulteplicity() {
		return multeplicity;
	}

	public static Properties loadProperties(PropertyMap propertiesFile) {
		Logger intLogger = LoggerFactory.getLogger(UMLElementsArchive.class);
		Properties props = new Properties();
		try {
			props.load(DepenciesParser.class.getClassLoader().getResourceAsStream(propertiesFile.getPropName()));
		} catch (IOException e) {
			if (intLogger.isWarnEnabled())
				intLogger.warn("Cannot retieve the " + propertiesFile, e);
		}
		return props;
	}

	/**
	 * Returns the value of a TaggedValue element.
	 * 
	 * @param valueTagged a {@link UMLValueTagged} element
	 * @param tagName the name of the <code>TaggedValue</code> element
	 * @return the value or <code>null</code> is the tag does not exist
	 */
	public String getTaggedValue(UMLValueTagged valueTagged, String tagName) {
		int index = valueTagged.getUmlTaggedValues().lastIndexOf(new UMLTaggedValueAdapter(tagName, null));
		return index > -1 ? valueTagged.getUmlTaggedValues().get(index).getValue() : null;
	}
	
	/**
	 * Verifies if the given valueTagged element has been defined as <code>Set<code>. This is done checking
	 * if the element owns a TaggedValue named {@link Dictionary#TYPE} whose value is formatted like
	 * <code>Set<One_Class_Name></code>.
	 * This kind of structure is usually found in classes attribute's whose stereotype is Union
	 * @param valueTagged the UMLValueTagged element 
	 **/
	public boolean isSetType(UMLValueTagged valueTagged) {
		String value = getTaggedValue(valueTagged, Dictionary.TYPE);
		if (value == null)
			return false;
		return (value.startsWith("Set<") || value.startsWith("Set&lt;"));
	}
	
	public Set<ClassModel> getClassModel() {
		return modelClasses;
	}
	
	public ClassModel getClassModelByUMLClass(UMLClassDataType umlClass) {
		UMLClassDataType intClass = umlClass;
		if (umlClass instanceof UMLDataType)
			intClass = ((UMLDataType)umlClass).getType();
		ClassModel cm = null;
		Iterator<ClassModel> iter = getClassModel().iterator();
		while (iter.hasNext()) {
			cm = iter.next();
			if (cm.getAssociatedClass().equals(intClass))
				return cm;
		}
		return null;
	}

	public ClassModel getClassModelByClassName(String className) {
		return getClassModelByUMLClass(getUMLClassByName(className));
	}
}
