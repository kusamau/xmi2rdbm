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
package ngn.services.newmoon.python;

import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.collect.CollectClassesClosure.CollectionType;
import ndg.services.newmoon.velocity.python.NmVelocityHelper;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLModel;
import ndg.services.newmoon.xmiModel.UML_ID;
import ndg.services.newmoon.xmiModel.adapter.UMLClassAdapter;
import ndg.services.newmoon.xmiModel.adapter.UMLModelAdapter;
import ndg.services.newmoon.xmiModel.adapter.UML_IDAdapder;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mnagni
 *
 */
public class NmVelocityHelperTest {


	
	@Test
	public final void testSubstituteDBType() {
		AttributeModel am = null;
		am = createAttributeModel("attr0", 0, 10, 50, false, CollectionType.NONE, "Vector");
		Assert.assertEquals("ARRAY(Text)", NmVelocityHelper.substituteDBType(am));
		am = createAttributeModel("attr0", 0, 1, 50, false, CollectionType.NONE, "Vector");
		Assert.assertEquals("ARRAY(Text)", NmVelocityHelper.substituteDBType(am));		
	}
	
	private UML_ID createUML_ID() {
		return new UML_IDAdapder(Integer.toString(RandomUtils.nextInt()), 
				Integer.toString(RandomUtils.nextInt()));
	}
	
	private UML_ID createUML_ID(String name) {
		return new UML_IDAdapder(Integer.toString(RandomUtils.nextInt()), 
				Integer.toString(RandomUtils.nextInt()), name);
	}
	
	private UMLModel createUMLModel() {
		return new UMLModelAdapter(createUML_ID(), null);
	}
	
	private UMLClass createUMLClass(String name) {
		return new UMLClassAdapter(createUML_ID(name), false, false, false, createUMLModel());
	}
	
	private ClassModel createClassModel(String umlClassName) {
		return new ClassModel(createUMLClass(umlClassName));
	}
	
	private AttributeModel createAttributeModel(String name, int lowerBound, Integer upperBound, 
			Integer lenght, boolean voidable, CollectionType collectionType, String umlClassName) {		
		return new AttributeModel(createClassModel(umlClassName), 
				name, lowerBound, upperBound, 
				lenght, voidable, collectionType);	
	}
}
