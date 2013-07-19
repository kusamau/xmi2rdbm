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
package ndg.services.newmoon.collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ndg.services.newmoon.NewmoonException;
import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.xmiModel.UMLClass;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author mnagni
 * 
 */
public class CollectClasses {
	
	private final List<UMLClass> umlClasses = new ArrayList<UMLClass>();
	private final UMLElementsArchive umlArchive;
	
	public CollectClasses(UMLElementsArchive umlArchive) {
		super();		
		this.umlArchive = umlArchive;
		this.umlClasses.addAll(this.umlArchive.getUMLElementByType(UMLClass.class));
	}

	public List<ClassModel> execute() throws IOException, NewmoonException {
		CollectClassesClosure cc = new CollectClassesClosure(umlArchive);
		CollectionUtils.forAllDo(umlClasses, cc);

		// Temporary commented
		/*
		 * if (!CollectionUtils.isEmpty(cc.getExceptions())) throw
		 * cc.getExceptions().get(0);
		 */

		return cc.getModels();
	}
}
