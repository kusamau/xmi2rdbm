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
package ndg.services.newmoon.velocity.python;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.collect.ClassModel.STEREOTYPE;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UMLStereotype;
import ndg.services.newmoon.xmiModel.UML_ID;
import ndg.services.newmoon.xmiModel.adapter.UML_IDAdapder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 *
 */
public class GenerateModuleHelper {

	private final File baseDir;
	
	private final Logger logger = LoggerFactory.getLogger(UMLElementsArchive.class);
	private final UMLElementsArchive umlArchive;
	
	public GenerateModuleHelper(File baseDir, UMLElementsArchive umlArchive) throws IOException {
		this.baseDir = baseDir;	
		this.umlArchive = umlArchive;
	}
	
	/**
	 * @return the baseDir
	 */
	public File getBaseDir() {
		return baseDir;
	}

	public void writeToFile(File file, String content) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write(content);
		fw.close();
		if (logger.isDebugEnabled())
			logger.debug(content);
	}
	
	public boolean isEnumLike(UMLClass umlClass) {
		if (STEREOTYPE.Boolean.name().equalsIgnoreCase(umlClass.getName()))
			return false;
		
		for (UMLStereotype stereotype : umlClass.getUmlStereotypes()) {
			UMLClass res = null;
			UML_ID umlId = null;
			if (stereotype.isReferenceId()) {
				umlId = umlArchive.getType(new UML_IDAdapder(stereotype.getId(), stereotype.getModelId()));
				if (umlId instanceof UMLClass)
					res = (UMLClass)umlId;
			}
			

			
			if (STEREOTYPE.Enumeration.name().equalsIgnoreCase(res.getName())
					|| STEREOTYPE.CodeList.name().equalsIgnoreCase(res.getName()))
				return true;
		}
		return false;
	}
	
	public File generatePythonPackage(UMLModel model) throws IOException {
		checkRootInit();
		
		File file = 
			model == null ? getBaseDir() :
					new File(getBaseDir(), StringUtils.replaceChars(model.toString(), '.', File.separatorChar).toLowerCase());
			
		if (!file.exists()) {
			FileUtils.forceMkdir(file);
			File parent = file;
			while (!parent.getAbsolutePath().equals(getBaseDir().getAbsolutePath())) {
				File initFile = new File(parent, "__init__.py");
				initFile.createNewFile();
				parent = parent.getParentFile();
			}
		}
		return file;
	}
	
	private void checkRootInit() throws IOException {
		File file = new File(getBaseDir(), "__init__.py");
		if (!file.exists())
			file.createNewFile();
	}
}
