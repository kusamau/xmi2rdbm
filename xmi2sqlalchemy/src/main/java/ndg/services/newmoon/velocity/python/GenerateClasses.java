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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import ndg.services.newmoon.NewmoonException;
import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.collect.ClassModel;

import org.apache.commons.collections.Closure;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 *
 */
public class GenerateClasses implements Closure {
	
	private final Logger logger = LoggerFactory.getLogger(GenerateClasses.class);
	private final static String templateFile = "velocity/python/pythonClass.vm";
	private List<NewmoonException> exceptions = new ArrayList<NewmoonException>();
	private final Context vcontext = NmVelocity.getInstance().createContext();
	private GenerateModuleHelper helper;	
	private final File baseDir;
	private final UMLElementsArchive umlArchive;
	
	/**
	 * @param baseDir
	 */
	public GenerateClasses(File baseDir, UMLElementsArchive umlArchive) {
		super();
		this.baseDir = baseDir;
		this.umlArchive = umlArchive;		
	}

	public enum CONTEXT {
		classModel, imports, stereotype, attributes
	};
	
	
	/* (non-Javadoc)
	 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
	 */
	@Override
	public void execute(Object input) {
		try {
			writeAll((ClassModel)input);
		} catch (IOException e) {
			exceptions.add(new NewmoonException(e));
		}
	}

	
	/**
	 * @return the helper
	 */
	private GenerateModuleHelper getHelper() {
		if (helper == null)
			try {
				helper = new GenerateModuleHelper(baseDir, umlArchive);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return helper;
	}
	
	private void writeAll(ClassModel cm) throws IOException {
			PythonClassModel pcm = new PythonClassModel(cm);
			if (pcm.skipIt()) {
				logger.info("Skipping python class generation for " + pcm.toString());
				return;
			}
					   
			vcontext.put(CONTEXT.imports.name(), pcm.generateImports());			
			vcontext.put(CONTEXT.stereotype.name(), pcm.getStereotypeImplementation());
			vcontext.put(CONTEXT.classModel.name(), pcm);	
			vcontext.put(CONTEXT.attributes.name(), pcm.getAttributes());
			File fl = new File(getHelper().generatePythonPackage(cm.getAssociatedClass().getUmlPackage()), cm.getAssociatedClass().getName().toLowerCase() + ".py");
			Writer sqlTablesWriter = new FileWriter(fl, false);
			sqlTablesWriter.append(NmVelocity.getInstance().fillTemplate(vcontext, templateFile));		
			sqlTablesWriter.close();			
	}
}
