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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import ndg.services.newmoon.UMLElementsArchive.PropertyMap;
import ndg.services.newmoon.xmiModel.EAStub;
import ndg.services.newmoon.xmiModel.UMLDependency;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLPackage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 * 
 */
public class DepenciesParser {

	// * The model to check */
	private final UMLModel model;
	
	private Properties xmiMaps = null;

	final static Logger logger = LoggerFactory.getLogger(DepenciesParser.class);
	private final UMLElementsArchive umlArchive;

	/**
	 * @param model
	 */
	public DepenciesParser(UMLModel model, UMLElementsArchive umlArchive) {
		super();
		this.model = model;
		this.umlArchive = umlArchive;
	}

	public void startProcess() throws NewmoonException {
		processModel(model);
	}

	public Properties getXMIMaps() {
		if (xmiMaps == null)
			xmiMaps = UMLElementsArchive.loadProperties(PropertyMap.XMI_MAP);
		return xmiMaps;		
	}	
	
	private void processModel(UMLModel model) throws NewmoonException {
		for (UMLDependency dependency : model.getUmlDependencies()) {
			processDependency(dependency);
		}
		for (UMLPackage pck : model.getUmlPackages()) {
			processModel(pck);
		}
	}

	/**
	 * Each XMI model may contains a number of elements which define how import
	 * external referenced resources (i.e. <EAStub> in Enterprise Architect)
	 * 
	 * This method verifies that during the recursive calls from one model to
	 * another elements with different xmi.id attribute value could be
	 * associated one with anoter on the base on a previously agreed mapping
	 * managed by the {@link UMLElementsArchive#existsComparableEAStub(EAStub)}.
	 * 
	 * @author mnagni
	 */
	private synchronized void processDependency(UMLDependency dependency) throws NewmoonException {
		
		if (umlArchive.getUmlModelByUMLDependency(dependency) != null)
			return;
		
		EAStub eaStub = umlArchive.getEAStubByUMLDependency(dependency);
		if (eaStub == null)
			return;

		if (!eaStub.getUmlType().equals("Package"))
			return;

		NmParser xmiParser;
		InputStream inputXML;
		if (umlArchive.getPakkages().contains(eaStub.getName()))
			return;

		inputXML = getXMIModelFromStub(eaStub);
		if (inputXML == null) {
			umlArchive.synchPackageElement(eaStub.getName(), false);
			return;
		}
		umlArchive.synchPackageElement(eaStub.getName(), true);
		try {
			xmiParser = new NmParser(inputXML, eaStub.getName(), umlArchive);
			umlArchive.startNewParser(xmiParser);
		} catch (Exception e) {
			logger.warn("Cannot load " + dependency, e);
		}
	}


	private InputStream getXMIModelFromStub(EAStub stub) throws NewmoonException {
		if (!stub.getUmlType().equals("Package"))
			return null;

		String xmiModel = getXMIMaps().getProperty(stub.getName().trim());
		if (xmiModel == null)
			return null;

		if (xmiModel.startsWith("http")) {
			return loadFromNetwork(xmiModel);
		} else {
			// load from internal repository
			return DepenciesParser.class.getClassLoader().getResourceAsStream(xmiModel);
		}
	}

	private InputStream loadFromNetwork(String xmiModel) throws NewmoonException {
		URL xmiURL = null;
		byte[] xmiFile = null;
		try {
			xmiURL = new URL(xmiModel);
			xmiFile = IOUtils.toByteArray(xmiURL.openStream());
		} catch (MalformedURLException e) {
			throw new NewmoonException(e);
		} catch (IOException e) {
			throw new NewmoonException(e);
		}
		return new ByteArrayInputStream(xmiFile);
	}
}
