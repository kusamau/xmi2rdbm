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

import java.util.Map;
import java.util.Set;

import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.collect.SuperAttribute;
import ndg.services.newmoon.velocity.python.support.ImportCollector;
import ndg.services.newmoon.velocity.python.support.TableModel;
import ndg.services.newmoon.xmiModel.UMLClassDataType;

import org.apache.commons.collections.Closure;

/**
 * @author  mnagni
 */
public class PythonImportClosure implements Closure {
		private ImportCollector importCollector = new ImportCollector();

		/* (non-Javadoc)
		 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
		 */
		@Override
		public void execute(Object input) {
			if (input == null)
				return; 
						
			if (input instanceof SuperAttribute) {
				SuperAttribute am = ((SuperAttribute)input);
				for (ClassModel cm : am.getSubTypes()) {
					importCollector.updateImports(((ClassModel)cm).getAssociatedClass());
				}
				return;
			}
			
			UMLClassDataType umlType = null;
			if (input instanceof UMLClassDataType) {
				umlType = (UMLClassDataType)input;
			} else if (input instanceof TableModel) {
				umlType = ((TableModel)input).getAssociatedClass();
			} else if (input instanceof AttributeModel) {
				AttributeModel am = ((AttributeModel)input);
				if (am.getAssociatedType() == null)
					return;
				umlType = am.getAssociatedType().getAssociatedClass();
			} else if (input instanceof ClassModel) {
				umlType = ((ClassModel)input).getAssociatedClass();
			}				
			importCollector.updateImports(umlType);
		}	
		
		public Map<String, Set<String>> getImportMap() {
			return importCollector.getImportMap();
		}
}