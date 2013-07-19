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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ndg.services.newmoon.xmiModel.UMLClass;

import org.apache.commons.collections.Closure;

/**
 * Retrieves {@link ClassModel}s from a internal collection. Looping over a collection
 * of {@link UMLClass} this <code>Closure</code> pick ups {@link ClassModel} instances 
 * from the <code>Collection</code> passed in the constructor which acts as reference collection.  
 * 
 * @author mnagni
 *
 */
public class CollectClassModel implements Closure {

	private final Collection<ClassModel> models;
	private final Set<ClassModel> result = new HashSet<ClassModel>();
	
	/**
	 * @param models the <code>ClassModel</code> reference collection
	 */
	public CollectClassModel(Collection<ClassModel> models) {
		super();
		this.models = models;
	}
	
	/**
	 * Returns the filtered <code>ClassModel</code>s
	 * @return the result
	 */
	public Set<ClassModel> getResult() {
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
	 */
	/**
	 * Core loop. The method does not process the input if it is not
	 * a {@link UMLClass} instance
	 * @param input the object to find in the reference collection
	 **/
	@Override
	public void execute(Object input) {
		if (input instanceof UMLClass) {
			process((UMLClass)input);	
		}
		
		if (input instanceof String) {
			process((String)input);	
		}
	}
	
	private void process(UMLClass umlClass) {
		ClassModel tm = new ClassModel(umlClass);			
		if (models.contains(tm)) {
			for (ClassModel model : models) {
				if (model.equals(tm)) {
					result.add(model);
					return;
				}						
			}
		}
	}
	
	private void process(String className) {
		for (ClassModel model : models) {
			if (model.getAssociatedClass().getName().equals(className)) {
				result.add(model);
				return;
			}						
		}
	}
}
