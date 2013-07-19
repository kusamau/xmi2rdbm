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
package ndg.services.newmoon;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.collect.CollectClasses;
import ndg.services.newmoon.collect.SuperAttribute;
import ndg.services.newmoon.exception.NM_ParamException;
import ndg.services.newmoon.velocity.python.GenerateClasses;
import ndg.services.newmoon.velocity.python.GenerateFromClassModel;
import ndg.services.newmoon.velocity.python.GenerateTables;
import ndg.services.newmoon.xmiModel.UMLModel;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 *
 */
public class XMIParser extends XMIParserAbstract {
	
	private final Logger logger = LoggerFactory.getLogger(XMIParser.class);
	
	public void execute(Map<NewmoonManager.NM_PARAM, String> params) throws NewmoonException {
		File baseDir = null;
		InputStream inputXML = null;
		File outDir = null;
		try {
			baseDir = new File(getParam(params, NewmoonManager.NM_PARAM.EXECUTION_DIR));
			inputXML = openInputStream(params, NewmoonManager.NM_PARAM.XMI_DOC, baseDir);
			outDir = new File(baseDir, getParam(params, NewmoonManager.NM_PARAM.OUTPUT_DIR));
			outDir.mkdir();			
		} catch (NM_ParamException ex) {
			throw new NewmoonException("Not valid parameter", ex);
		}
		
		InputStream inputAddClass = null;
		InputStream constraints = null;
		Set<String> additionalClasses = null;
		try {
			inputAddClass = openInputStream(params, NewmoonManager.NM_PARAM.XMI_ADD_CLASSES, baseDir);
			additionalClasses = NmParserHelper.getCSVFromInputStream(inputAddClass);
		} catch (NM_ParamException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Additional Classes file not loaded");	
			}
		}
		
		try {
			constraints = openInputStream(params, NewmoonManager.NM_PARAM.XMI_ADD_CONSTRAINTS, baseDir);			
		} catch (NM_ParamException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Not valid parameter", ex);	
			}
		}
		
		try {
			UMLElementsArchive umlArchive = new UMLElementsArchive();
			NmParser xmiParser = new NmParser(inputXML, null, umlArchive);		
			Future<UMLModel> myModel = umlArchive.startNewParser(xmiParser);			
			umlArchive.waitParserToComplete();			
			
			/*  I try to generate just what I need */
			UMLModel umlModel = myModel.get();			
			
			//Generates the ClassModels
			CollectClasses cc = new CollectClasses(umlArchive);
			List<ClassModel> cms = cc.execute();
			umlArchive.getClassModel().addAll(cms);
		
			applyConstrains(constraints, umlArchive);
			
			//Update the ClassModels substituting the SuperAttributes when necessary
			substitutesSuperAttributes(cms);
				
			//generates library and schema for RDBM
			XMIToRDBM.execute(outDir, umlArchive, additionalClasses, umlModel);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
	}
	
	private void applyConstrains(InputStream constraints, UMLElementsArchive umlArchive) throws NewmoonException {
		try {
			List<String> rules = IOUtils.readLines(constraints);
			String[] template;
			for (String rule : rules) {
				template = StringUtils.split(rule, ":");
				if (template.length != 3) {
					logger.error("Constraint rule not appliable: " + rule);
					continue;
				}
				ClassModel target = umlArchive.getClassModelByClassName(template[0]);
				ClassModel constrain = umlArchive.getClassModelByClassName(template[2]);
				if (target == null || constrain == null) {
					throw new NewmoonException("Cannot apply constrain rule because target--> " 
							+ template[0] + ":" + target + " constrain--> " + template[2] + ":" + constrain);
				}
				for (AttributeModel am : target.getAttributeModel()) {
					if (am.getName().equals(template[1])) {
						am.setAssociatedType(constrain);
					}
				}
			}
		} catch (IOException ex) {
			logger.error("Cannot apply constraints", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void substitutesSuperAttributes(List<ClassModel> cms) {
		//creates a map between parents and childrens (classes and their subclasses)
		Map<ClassModel, Set<ClassModel>> pc = new HashMap<ClassModel, Set<ClassModel>>();
		for (ClassModel cm : cms) {			
			for(ClassModel cmp : cm.getParents()) {
				if (!pc.containsKey(cmp)) {
					pc.put(cmp, new HashSet<ClassModel>());
				}
				pc.get(cmp).add(cm);
			}
		}
		
		//removes the item which has zero or one mapping 
		StringBuffer sb = new StringBuffer();
		Iterator<ClassModel> iter = pc.keySet().iterator();
		while(iter.hasNext()) {
			ClassModel umlClass = iter.next();
			if (pc.get(umlClass).size() <= 1) {
				iter.remove();
				continue;
			}		
			sb.append(umlClass.getAssociatedClass().getName());
			sb.append(": ");			
			for (ClassModel ic : pc.get(umlClass)) {
				sb.append(ic.getAssociatedClass().getName());
				sb.append(", ");				
			}
			sb.append("\n");
			logger.info(sb.toString());
			sb.delete(0, sb.length());
		}
		
		//
		Map<ClassModel, Set<ClassModel>> classUsers = new HashMap<ClassModel, Set<ClassModel>>();
		for (ClassModel cm : cms) {
			if (cm.isUnion()) {
				continue;
			}
			Iterator<AttributeModel> iterAttr = cm.getAttributeModel().iterator();
			Set<AttributeModel> sas = new HashSet<AttributeModel>();
			//Done in this way because seems I cannot trust the iterAttr.remove() method
			Set<AttributeModel> toRemove = new HashSet<AttributeModel>();
			AttributeModel attr = null;
			while (iterAttr.hasNext()) {
				attr = iterAttr.next();
				if (attr == null 
						|| attr.getAssociatedType() == null 
						|| attr.getAssociatedType().getAssociatedClass() == null
						|| !pc.containsKey(attr.getAssociatedType())
						|| attr instanceof SuperAttribute
						|| attr.getAssociatedType().getAssociatedClass().getUmlPackage().toString().contains("19103")) {
					continue;
				}

				if (!classUsers.containsKey(attr.getAssociatedType())) {
					classUsers.put(attr.getAssociatedType(), new HashSet<ClassModel>());
				}				
				classUsers.get(attr.getAssociatedType()).add(cm);
				
				SuperAttribute sa = new SuperAttribute(attr);			
				sa.getSubTypes().addAll(pc.get(attr.getAssociatedType()));
				toRemove.add(attr);
				sas.add(sa);				
			}
			sas.addAll(ListUtils.removeAll(cm.getAttributeModel(), toRemove));
			cm.getAttributeModel().clear();
			cm.getAttributeModel().addAll(sas);
		}
		logger.info("\n Users list \n");
		iter = classUsers.keySet().iterator();
		while(iter.hasNext()) {
			ClassModel umlClass = iter.next();
			sb.append(umlClass.getAssociatedClass().getName());
			sb.append(": ");			
			for (ClassModel ic : classUsers.get(umlClass)) {
				sb.append(ic.getAssociatedClass().getName());
				sb.append(", ");				
			}
			sb.append("\n");
			logger.info(sb.toString());
			sb.delete(0, sb.length());
		}
	} 
}
