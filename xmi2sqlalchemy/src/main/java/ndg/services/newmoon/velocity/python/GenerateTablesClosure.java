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
package ndg.services.newmoon.velocity.python;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ndg.services.newmoon.NewmoonException;
import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.collect.SuperAttribute;
import ndg.services.newmoon.velocity.python.support.AssociationTable;
import ndg.services.newmoon.velocity.python.support.ForeignKey;
import ndg.services.newmoon.velocity.python.support.Relationship;
import ndg.services.newmoon.velocity.python.support.Relationship.RELATION_TYPE;
import ndg.services.newmoon.velocity.python.support.TableModel;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLClassDataType;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 * 
 */
public class GenerateTablesClosure implements Closure {
	private final Set<TableModel> tableModels = new HashSet<TableModel>();
	private final Set<AssociationTable> associationTable = new HashSet<AssociationTable>();
	private final Set<TableModel> notSortable = new HashSet<TableModel>();
	private List<NewmoonException> exceptions = new ArrayList<NewmoonException>();

	private final UMLElementsArchive umlArchive;
	
	public GenerateTablesClosure(UMLElementsArchive umlArchive) {
		super();
		this.umlArchive = umlArchive;
	}
	
	private final Logger logger = LoggerFactory
			.getLogger(GenerateTablesClosure.class);

	/**
	 * @return the tableModels
	 */
	Set<TableModel> getTableModels() {
		return tableModels;
	}

	/**
	 * @return the associationTable
	 */
	Set<AssociationTable> getAssociationTable() {
		return associationTable;
	}

	/**
	 * @return the notSortable
	 */
	Set<TableModel> getNotSortable() {
		return notSortable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
	 */
	@Override
	public void execute(Object input) {
		try {
			processUMLclass((ClassModel) input);
		} catch (NewmoonException e) {
			exceptions.add(e);
		} catch (IOException e) {
			exceptions.add(new NewmoonException(e));
		}
	}

	public void transformToManyToMany(Relationship rl) {
		rl.setRelationType(RELATION_TYPE.MANY_TO_MANY);		
		rl.setAssociationTable(getAssociationTable(rl));
		getAssociationTable().add(rl.getAssociationTable());
		rl.getToTable().getFk().remove(rl.getAssociatedForeignKey());
		rl.setAssociatedForeignKey(null);			
	}	
	
	private ClassModel getClassModel(UMLClassDataType umlClass) {
		return umlArchive.getClassModelByUMLClass(umlClass);
	}

	private TableModel processUMLclass(ClassModel classModel)
			throws NewmoonException, IOException {
		
		if (checkIfSkipProcess(classModel))
			return null;
		
		if (tableModelExists(classModel))
			return getTableModel(classModel);

		TableModel tm = new TableModel(classModel);
		tableModels.add(tm);
		collectData(tm);
		return tm;
	}

	private void collectData(TableModel tm) throws NewmoonException,
			IOException {
		parseAttributes(tm);
		collectInheritedParents(tm);
		addInheritanceAsComposition(tm);
	}
	
	private void parseAttributes(TableModel parent) throws NewmoonException,
			IOException {
		AttributeModel am = null;
		Iterator<AttributeModel> iter = parent.getAssociatedClassModel().getAttributeModel().iterator();
		while (iter.hasNext()) {
			am = iter.next();
			
			if (am instanceof SuperAttribute){
				for (ClassModel cm : ((SuperAttribute)am).getSubTypes()) {
					String attrName = String.format("_%1$s_%2$s", am.getName(), cm.getAssociatedClass().getName().toLowerCase());
					processParentChildAttribute(parent, processUMLclass(cm), attrName , am.hasMultiplicity(), am.isNullable());
				}
				continue;
			}
			
			ClassModel cm = am.getAssociatedType();
			if (cm == null || checkIfSkipProcess(cm)) {
				parent.getAttributeModel().add(am);
				continue;
			}
			processParentChildAttribute(parent, processUMLclass(cm), am.getName(), am.hasMultiplicity(), am.isNullable());
		}
	}
	
	private void processParentChildAttribute(TableModel parent, TableModel child, String attributeName, boolean hasMultiplicity, boolean isNullable) {
		if (child == null)
			return;		

		Relationship rl = new Relationship(parent, child, NmVelocityHelper.checkName(attributeName), isNullable, hasMultiplicity);

		if (parent.equals(child)) {
			if (hasMultiplicity) {
				transformToManyToMany(rl);	
			}
		} else {
			ForeignKey fk = new ForeignKey(parent, attributeName);
			child.getFk().add(fk);
			if (hasMultiplicity) {
				rl.setRelationType(RELATION_TYPE.ONE_TO_MANY);
			} else {
				rl.setRelationType(RELATION_TYPE.ONE_TO_ONE);
				rl.setBackrefName(parent.getAssociatedClass().getName().toLowerCase() + "_" + rl.getName());
			}
			parent.getRelationship().add(rl);
			associateFKandRelationship(fk, rl);
		}		
	}
	
	private void associateFKandRelationship(ForeignKey fk, Relationship relationship) {
		fk.setAssociatedRelationship(relationship);
		relationship.setAssociatedForeignKey(fk);
	}
	
	private void addInheritanceAsComposition(TableModel child) throws NewmoonException, IOException {
		UMLClass umlClass = child.getAssociatedClass();		
		for (ClassModel cm : getClassModel(umlClass).getParents()) {
			if (cm == null || checkIfSkipProcess(cm)) 
				continue;

			TableModel parent = processUMLclass(cm);
			String relName = "_" + parent.getAssociatedClass().getName().toLowerCase();
			Relationship rl = new Relationship(child, parent, relName, false, false);			
			ForeignKey fk = new ForeignKey(child, relName);
			parent.getFk().add(fk);
			rl.setRelationType(RELATION_TYPE.ONE_TO_ONE);
			rl.setBackrefName(child.getAssociatedClass().getName().toLowerCase() + "_" + relName);
			child.getRelationship().add(rl);
			associateFKandRelationship(fk, rl);
		}
	}	
	
	private boolean checkIfSkipProcess(ClassModel classModel) {
		return (classModel.isCodeList() 
				|| classModel.isEnumeration()
				|| NmVelocityHelper.isExternallyMapped(classModel.getAssociatedClass().getName()));
	}


	
	private boolean tableModelExists(ClassModel classModel) {
		return tableModels.contains(new TableModel(classModel));
	}

	private TableModel getTableModel(ClassModel classModel) {
		TableModel tm = new TableModel(classModel);
		if (tableModels.contains(tm)) {
			for (TableModel model : tableModels) {
				if (model.equals(tm))
					return model;
			}
		}
		return tm;
	}

	private void collectInheritedParents(TableModel tm)
			throws NewmoonException, IOException {
		Iterator<ClassModel> iter = getClassModel(tm.getAssociatedClass())
				.getParents().iterator();
		while (iter.hasNext()) {
			CollectionUtils.addIgnoreNull(tm.getInherited(),
					processUMLclass(iter.next()));
		}
	}
	
	private AssociationTable getAssociationTable(Relationship rl) {
		AssociationTable ret = new AssociationTable(rl);
		if (getAssociationTable().contains(ret)) {
			for (AssociationTable at : getAssociationTable()) {
				if (at.equals(ret)) {
					return at;
				}
			}
		}
		return ret;
	}
}
