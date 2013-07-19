package ndg.services.newmoon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ndg.services.newmoon.xmiModel.EAStub;
import ndg.services.newmoon.xmiModel.UMLAssociation;
import ndg.services.newmoon.xmiModel.UMLAssociationEnd;
import ndg.services.newmoon.xmiModel.UMLAttribute;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLClassifierRole;
import ndg.services.newmoon.xmiModel.UMLCollaboration;
import ndg.services.newmoon.xmiModel.UMLDataType;
import ndg.services.newmoon.xmiModel.UMLDependency;
import ndg.services.newmoon.xmiModel.UMLGeneralization;
import ndg.services.newmoon.xmiModel.UMLInterface;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLPackage;
import ndg.services.newmoon.xmiModel.UMLStereotyped;
import ndg.services.newmoon.xmiModel.UMLTaggedValue;
import ndg.services.newmoon.xmiModel.UMLValueTagged;
import ndg.services.newmoon.xmiModel.UML_Element;
import ndg.services.newmoon.xmiModel.UML_ID;
import ndg.services.newmoon.xmiModel.adapter.UML_IDAdapder;
import ndg.services.newmoon.xmiModel.v11.Dictionary;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;


public class NmParser implements Callable<UMLModel> {

	public static final String UML_NS = "omg.org/UML1.3";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	static final ErrorHandler eh = new NmErrorHandler();
	static final boolean VALIDATE_DOCUMENT = false;

	private final NmParserHelper helper = new NmParserHelper();
	private final byte[] inputXML;
	private final String keyName;
	private UMLModel mainUmlModel;
	final Logger logger = LoggerFactory.getLogger(NmParser.class);
	private final ModelXMIFactory modelFactory = new ModelXMI11Factory();
	private final String XMI_DOCUMENT_IDENTIFICATOR = Long.toString(Calendar.getInstance().getTimeInMillis());
	private final UMLElementsArchive umlArchive;

	/**
	 * Generates the python/SQL code from an XMI model
	 * @param packageBase the folder into which python module will be written
	 * @param inputXML the XMI document
	 * @throws IOException if problems occurs with document parsing 
	 **/
	public NmParser(InputStream inputXML, String keyName, UMLElementsArchive umlArchive) throws IOException {
		this.inputXML = IOUtils.toByteArray(inputXML);
		this.keyName = keyName;
		this.umlArchive = umlArchive;
	}
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public UMLModel call() throws Exception {
		// TODO Auto-generated method stub
		return processXMI();
	}

	private Document parseXmlFile() {		
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				
		//Otherwise spaces between two nodes will be evaluated as (text) node too
		dbf.setValidating(VALIDATE_DOCUMENT); //mandatory for the following
		dbf.setIgnoringElementContentWhitespace(true);		
		
		//Otherwise no namespace support will be given
		dbf.setNamespaceAware(true);		
		dbf.setIgnoringComments(true);
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(eh);
			db.setEntityResolver(new NmEntityResolver());
			
			// parse using builder to get DOM representation of the XML file
			return db.parse(new ByteArrayInputStream(inputXML));
		} catch (ParserConfigurationException e) {
			doLogError(e);
		} catch (SAXException e) {
			doLogError(e);
		} catch (IOException e) {
			doLogError(e);
		}
		return null;
	}
	
	private UMLModel processXMI() throws NewmoonException {
		Document doc = parseXmlFile();		
		processXMI(doc.getDocumentElement());
		
		DepenciesParser dp = new DepenciesParser(mainUmlModel, umlArchive);
		dp.startProcess();
		processGeneralizations();
		processAssociations();
		return mainUmlModel;
	}
	
	private void processXMI(Node node) throws NewmoonException{	
		if (node == null)
			return;		

		Node tnode = helper.getFirstSiblingNoTextNode(node.getFirstChild());
		//processes the EAStubs with an higher proprity		
		while (tnode != null) {
			if (tnode.getLocalName().equals("XMI.extensions")) {
				processXMIExtension(tnode);			
			}
			tnode = helper.getFirstSiblingNoTextNode(tnode.getNextSibling());
		}
		
		tnode = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (tnode != null) {
			if (tnode.getLocalName().equals("XMI.content")) {
				processXMIContent(tnode);			
			}
			tnode = helper.getFirstSiblingNoTextNode(tnode.getNextSibling());
		}
	}	
	
	private void processXMIContent(Node node) throws NewmoonException {
		if (node == null)
			return;		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("Model")) {
				validateNode(node, "Model", UML_NS);
				processModel(node);			
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}
	
	private void processXMIExtension(Node node){
		if (node == null)
			return;		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("EAStub")) {
				processEAStub(node);			
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}
	
	private void processEAStub(Node node) {
		EAStub stub = modelFactory.createEAStub((Element)node, XMI_DOCUMENT_IDENTIFICATOR);
		appendToArchive(stub);
	}	
	
	private UMLModel processModel(Node node) throws NewmoonException {
		validateNode(node, "Model", UML_NS);
	
		UMLModel model = modelFactory.createUMLModel((Element)node, XMI_DOCUMENT_IDENTIFICATOR, null);
		
		UMLModel ret = umlArchive.getUMLModelDefinition(model);
		if (ret != null) {
			ret.getKeyNames().add(keyName);
			return (UMLModel)ret;		
		} else {
			mainUmlModel = model;	
			mainUmlModel.getKeyNames().add(keyName);
			appendToArchive(model);
		}
		
		//else is a now model		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {			
			if (node.getLocalName().equals("Namespace.ownedElement")) {
				validateNode(node, "Namespace.ownedElement", UML_NS);
				processOwnElement(node, model);			
			}			
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}	
		return model;
	}	

	private void processOwnElement(Node node, UMLModel umlModel) throws NewmoonException {
		if (node == null)
			return;
		
		validateNode(node, "Namespace.ownedElement", UML_NS);

		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());
		UML_ID ret = null;
		while (node != null) {		
			if (umlModel instanceof UMLPackage) {
				ret = processPackageOwnElement(node, (UMLPackage) umlModel);				
			} 
			
			if (node.getLocalName().equals("Class")) {
				validateNode(node, "Class", UML_NS);
				ret = processClass(node, umlModel);		
			}
			
			if (node.getLocalName().equals("Package")) {
				validateNode(node, "Package", UML_NS);
				ret = processPackage(node, umlModel);			
			}
			
			if (node.getLocalName().equals("DataType")) {
				validateNode(node, "DataType", UML_NS);
				ret = processDataType(node, umlModel);			
			}

			if (node.getLocalName().equals("Generalization")) {
				validateNode(node, "Generalization", UML_NS);
				ret = collectGeneralization(node);			
			}
			
			if (node.getLocalName().equals("Association")) {
				validateNode(node, "Association", UML_NS);
				ret = collectAssociation(node);			
			}			

			appendToArchive(ret);
			
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}		
	}
	
	private UML_ID processPackageOwnElement(Node node, UMLPackage umlPackage) throws NewmoonException {
		if (node.getLocalName().equals("Dependency")) {
			validateNode(node, "Dependency", UML_NS);
			return processDependency(node, umlPackage);			
		}
		
		if (node.getLocalName().equals("Interface")) {
			validateNode(node, "Interface", UML_NS);
			return processInterface(node, umlPackage);			
		}
		
		if (node.getLocalName().equals("Collaboration")) {
			validateNode(node, "Collaboration", UML_NS);
			return processCollaboration(node, umlPackage);			
		}
		return null;
	}	

	
	
	private void processCollaborationOwnElement(Node node, UMLCollaboration umlCollaboration) throws NewmoonException {
		if (node == null)
			return;
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("ClassifierRole")) {
				validateNode(node, "ClassifierRole", UML_NS);
				processClassifierRole(node);			
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());			
		}
	}	
	
	private UMLInterface processInterface(Node node, UMLPackage parent) {
		return modelFactory.createUMLInterface((Element)node, getMainModelId(), parent);
		/* No further processing is done because this element is not necessary for the data model encoding */
	}			
	
	private void processClassifierRole(Node node) {
		UMLClassifierRole cr = modelFactory.createUMLClassifierRole((Element)node, getMainModelId());	
		appendToArchive(cr);
	}		
	
	private UMLDataType processDataType(Node node, UMLModel umlModel) {
		//Process the DataType element
		/*Actually I set the 'type' parameter to null because the
		 * uml:dataType I met are defined just on their name,say Set<GF_AttributeType>
		 * as consequence I cannot realistically set the type in the better way.
		 * Probably at the ClassModel level process it would be possible to update such value 
		 */
		UMLDataType umlDT = modelFactory.createUMLDataType((Element)node, getMainModelId(), umlModel, null);
		umlModel.getUmlDataType().add(umlDT);
		return umlDT;
	}
	
	private UMLCollaboration processCollaboration(Node node, UMLPackage umlPackage) throws NewmoonException {
		//Process the Collaboration element
		UMLCollaboration umlCollaboration = modelFactory.createUMLCollaboration((Element)node, getMainModelId());
		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("Namespace.ownedElement")) {
				validateNode(node, "Namespace.ownedElement", UML_NS);
				processCollaborationOwnElement(node, umlCollaboration);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
		
		return umlCollaboration;
	}	
	
	
	private UMLDependency processDependency(Node node, UMLPackage umlPackage) {
		//Process the Dependency element
		UMLDependency umlPkg = modelFactory.createUMLDependency((Element)node, getMainModelId());
		umlPackage.getUmlDependencies().add(umlPkg);
		return umlPkg;
	}	
	
	private UMLPackage processPackage(Node node, UMLModel umlModel) throws NewmoonException {
		//Process the Package element
		UMLPackage umlPkg = modelFactory.createUMLPackage((Element)node, getMainModelId(), umlModel);
		
		logger.debug("Now processing Package: " + umlPkg.getName() + " Model: " + umlModel.getName());
		
		umlArchive.synchPackageElement(umlPkg.getName(), true);
		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("Namespace.ownedElement")) {
				validateNode(node, "Namespace.ownedElement", UML_NS);
				processOwnElement(node, umlPkg);
			}
			
			if (node.getLocalName().equals("ModelElement.stereotype")) {
				validateNode(node, "ModelElement.stereotype", UML_NS);
				processStereotype(node, umlPkg);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		} 
		return umlPkg;
	}
	
	private void processStereotype(Node node, UMLStereotyped owner) throws NewmoonException{
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("Stereotype")) {
				validateNode(node, "Stereotype", UML_NS);
				modelFactory.createUMLStereotype((Element)node, getMainModelId(), owner);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		} 
	}
	
	private UMLGeneralization collectGeneralization(Node node) throws NewmoonException {
		return modelFactory.createUMLGeneralization((Element)node, getMainModelId());		
	}	
	
	private UMLAssociation collectAssociation(Node node) throws NewmoonException {
		UMLAssociation umlAssociation 
			= modelFactory.createUMLAssociation((Element)node, getMainModelId());
		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("Association.connection")) {
				validateNode(node, "Association.connection", UML_NS);
				processAssociationConnection(node, umlAssociation);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
		return umlAssociation;
	}	
	
	private void processAssociationConnection(Node node, UMLAssociation umlAssociation) throws NewmoonException {
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("AssociationEnd")) {
				validateNode(node, "AssociationEnd", UML_NS);
				processAssociationEnd(node, umlAssociation);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}	
	
	private void processAssociationEnd(Node node, UMLAssociation umlAssociation) throws NewmoonException {
		UMLAssociationEnd umlAssociationEnd 
			= modelFactory.createUMLAssociationEnd((Element)node, getMainModelId(), umlAssociation);
		umlAssociation.getAssociationEnds().add(umlAssociationEnd);
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("ModelElement.taggedValue")) {
				validateNode(node, "ModelElement.taggedValue", UML_NS);
				processModelElement(node, umlAssociationEnd);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}	
	
	public UMLClass processClass(Node node, UMLModel umlModel) throws NewmoonException {
		UMLClass umlClass = modelFactory.createUMLClass((Element)node, getMainModelId(), umlModel);
		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("Classifier.feature")) {
				validateNode(node, "Classifier.feature", UML_NS);
				processClassifierFeature(node, umlClass);
			}
			if (node.getLocalName().equals("ModelElement.stereotype")) {
				validateNode(node, "ModelElement.stereotype", UML_NS);
				processStereotype(node, umlClass);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
		return umlClass;
	}	
	
	private void processClassifierFeature(Node node, UMLClass umlClass) throws NewmoonException {
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());		
		while (node != null) {
			if (node.getLocalName().equals("Attribute")) {
				validateNode(node, "Attribute", UML_NS);
				processAttribute(node, umlClass);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}
	
	private void processAttribute(Node node, UMLClass umlClass) throws NewmoonException {
		UMLAttribute umlAttribute = modelFactory.createUMLAttribute((Element)node, umlClass);				

		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());
		while (node != null) {
			if (node.getLocalName().equals("StructuralFeature.type")) {
				validateNode(node, "StructuralFeature.type", UML_NS);
				processStructuralFeature(node, umlAttribute);
			}
			if (node.getLocalName().equals("ModelElement.stereotype")) {
				validateNode(node, "ModelElement.stereotype", UML_NS);
				processStereotype(node, umlAttribute);
			}
			if (node.getLocalName().equals("ModelElement.taggedValue")) {
				validateNode(node, "ModelElement.taggedValue", UML_NS);
				processModelElement(node, umlAttribute);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}	
	
	private void processModelElement(Node node, UMLValueTagged valueTagOwner) throws NewmoonException {		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());
		UMLTaggedValue taggedValue = null;
		while (node != null) {
			if (node.getLocalName().equals("TaggedValue")) {
				validateNode(node, "TaggedValue", UML_NS);
				taggedValue = modelFactory.createUMLTaggedValue((Element)node);
				valueTagOwner.getUmlTaggedValues().add(taggedValue);
			}
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}	
	
	private void processStructuralFeature(Node node, UMLAttribute umlAttribute) throws NewmoonException {		
		node = helper.getFirstSiblingNoTextNode(node.getFirstChild());
		while (node != null) {
			if (node.getLocalName().equals("Classifier")) {
				validateNode(node, "Classifier", UML_NS);
				Node name = node.getAttributes().getNamedItem(Dictionary.NAME);
				umlAttribute.setClassifier(new UML_IDAdapder(
						node.getAttributes().getNamedItem(Dictionary.XMI_IDREF) != null ? 
								node.getAttributes().getNamedItem(Dictionary.XMI_IDREF).getNodeValue() 
									: node.getAttributes().getNamedItem(Dictionary.XMI_ID).getNodeValue(),
									getMainModelId(),
						name != null ? name.getNodeValue() : null,
						node.getAttributes().getNamedItem(Dictionary.XMI_IDREF) != null));
			}
			
			node = helper.getFirstSiblingNoTextNode(node.getNextSibling());
		}
	}	 
	
	private void validateNode(Node node, String name, String namespace) throws NewmoonException {
		String wrongNSmsg = "Shold be have namespace %1 but is %2";
		String wrongNodemsg = "Shold be a %1:%2 but is a %3:%4";
		if (!node.getNamespaceURI().equals(namespace))
			throw new NewmoonException(String.format(wrongNSmsg, namespace, node.getNamespaceURI()));
		
		if (!node.getLocalName().equals(name))
			throw new NewmoonException(String.format(wrongNodemsg, namespace, name, node.getNamespaceURI(), node.getLocalName()));
	}
	
	public NodeList getElementsByTagName(Element element, String elName) {
		return element.getElementsByTagName(elName);
	}
	public void printElementAttribute(NodeList nl, String elName) {
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the class element
				Element el = (Element) nl.item(i);

				System.out.println(el.getAttribute(elName));
			}
		}
	}
	
	private void doLogError(Throwable ex) {
		if (logger.isErrorEnabled()) {
			logger.error("Error", ex);
		}
	}
	
	/**
	 * By default append each new instance to the {@link UMLElementsArchive}
	 * */
	private boolean appendToArchive(UML_ID element) {
		if (element == null)
			return false;
		
		return !element.isReferenceId() ?
				umlArchive.appendElement(element) : false;
	}	
	
	private String getMainModelId() {
		return XMI_DOCUMENT_IDENTIFICATOR;
		//return mainUmlModel == null ? null : mainUmlModel.getModelId();
	} 
	

	private void processGeneralizations() {
		List<UMLGeneralization> generalizations = umlArchive.getUMLElementByType(UMLGeneralization.class);
		for (UMLGeneralization generalization : generalizations) {
			UML_Element subtype = umlArchive.getType(generalization.getSubtype());
			if (subtype instanceof UMLClass) {
				((UMLClass)subtype).getGeneralization().add(generalization);
			}
		}
	}
	
	private void processAssociations() {
		List<UMLAssociation> associations = umlArchive.getUMLElementByType(UMLAssociation.class);
		for (UMLAssociation association : associations) {
			if (association.getAssociationEnds().size() == 2) {
				UMLAssociationEnd[] ends = association.getAssociationEnds().toArray(new UMLAssociationEnd[0]);
				processEndAssociations(ends[0], ends[1]);
			}
		}
	}
	
	private void processEndAssociations(UMLAssociationEnd source, UMLAssociationEnd target) {
		UML_Element elSource = umlArchive.getType(source.getType());
		UML_Element elTarget = umlArchive.getType(target.getType());
		if (StringUtils.isNotEmpty(target.getName()) && elSource instanceof UMLClass) {
			CollectionUtils.addIgnoreNull(((UMLClass)elSource).getAssociationEnds(), target);
		}
		if (StringUtils.isNotEmpty(source.getName()) && elTarget instanceof UMLClass) {
			CollectionUtils.addIgnoreNull(((UMLClass)elTarget).getAssociationEnds(), source);
		}			
	}
}
