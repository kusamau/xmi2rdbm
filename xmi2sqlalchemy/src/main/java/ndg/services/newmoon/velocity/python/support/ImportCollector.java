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
package ndg.services.newmoon.velocity.python.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ndg.services.newmoon.NmParserHelper;
import ndg.services.newmoon.velocity.python.NmVelocityHelper;
import ndg.services.newmoon.xmiModel.UMLClassDataType;

import org.apache.commons.collections.CollectionUtils;

/**
 * Collects all the imports necessary for the to-be-created python class.
 * 
 * 
 * @author mnagni
 *
 */
public class ImportCollector {
	private Map<String, Set<String>> importMap = new HashMap<String, Set<String>>();

	/**
	 * Updates the classes to import. If enabled, checks if the import should be avoided or not
	 * using the {@link GenerateModuleHelper.skipElement(umlClass)} method.
	 *   
	 * @param umlClass The class to import 
	 **/
	public <T extends UMLClassDataType> void updateImports(T umlClass) {
		if (umlClass == null || umlClass.getName().equals("EARootClass"))
			return;

		String moduleName = null;
		String className = null;
		if (NmVelocityHelper.isExternallyMapped(umlClass.getName())) {
			String[] imp = NmVelocityHelper.formatPythonImportString(umlClass);
			moduleName = imp[0];
			className = imp[1];
		} else {
			moduleName = NmVelocityHelper.getPythonModule(umlClass);
			className = umlClass.getName();
		}			
		updateImportMap(moduleName, className);
	}

	private void updateImportMap(String moduleName, String className) {
		if (!importMap.containsKey(moduleName)) {
			importMap.put(moduleName, new HashSet<String>());
		}
		CollectionUtils.addIgnoreNull(importMap.get(moduleName), className);		
	}
	
	public Map<String, Set<String>> getImportMap() {
		return importMap;
	}
	
	public void clear() {
		importMap.clear();
	}
}
