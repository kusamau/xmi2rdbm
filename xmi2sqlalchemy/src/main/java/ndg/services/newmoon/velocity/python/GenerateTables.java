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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.CollectClassModel;
import ndg.services.newmoon.velocity.python.support.AssociationTable;
import ndg.services.newmoon.velocity.python.support.ForeignKey;
import ndg.services.newmoon.velocity.python.support.OverriddenAttribute;
import ndg.services.newmoon.velocity.python.support.Relationship;
import ndg.services.newmoon.velocity.python.support.Relationship.RELATION_TYPE;
import ndg.services.newmoon.velocity.python.support.TableModel;
import ndg.services.newmoon.xmiModel.UMLModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 * 
 */
public class GenerateTables {

	private static int MAX_DEPTH = 0;

	private enum CONTEXT {
		tableModel, umlClass, simpleAttributes, imports, mapping, inheritedParents, associationTable, notSortable
	};

	private final static String tableTemplateFile = "velocity/python/sqlAlchemyTables.vm";

	private final StrBuilder sqlTablesStrBuffer = new StrBuilder(1000);

	// Collects the necessary classes to import
	private final static String importTemplateFile = "velocity/python/importing.vm";
	// Collects the information about the relations between the mappers
	private final static String mapperTemplateFile = "velocity/python/mapping.vm";
	// Collects the information about the relations between the not sortable
	// mappers (first step)
	private final static String notSortableMapperTemplateFile_1 = "velocity/python/notSortableMapping_1.vm";
	// Collects the information about the relations between the not sortable
	// mappers (second step)
	private final static String notSortableMapperTemplateFile_2 = "velocity/python/notSortableMapping_2.vm";
	// Collects the information to build a manytomany adapter table
	private final static String associationTemplateFile = "velocity/python/association.vm";

	private final Context vcontext = NmVelocity.getInstance().createContext();
	private GenerateModuleHelper helper;

	private final Logger logger = LoggerFactory.getLogger(GenerateTables.class);

	private final File baseDir;
	private final UMLElementsArchive umlArchive;
	private final Set<String> additionalClasses = new HashSet<String>();

	/**
	 * @param baseDir
	 */
	public GenerateTables(File baseDir, UMLElementsArchive umlArchive) {
		this(baseDir, umlArchive, new HashSet<String>());
	}

	public GenerateTables(File baseDir, UMLElementsArchive umlArchive, Set<String> additionalClasses) {
		super();
		this.baseDir = baseDir;
		this.umlArchive = umlArchive;
		if (additionalClasses != null) {
			this.additionalClasses.addAll(additionalClasses);	
		}
	}
	
	public void execute(UMLModel umlModel) throws IOException {
		// Extracts the ClassModels belonging only to the given UMLModel
		CollectClassModel ccm = new CollectClassModel(umlArchive.getClassModel());
		CollectionUtils.forAllDo(umlArchive.getUMLClassesByUMLModel(umlModel), ccm);

		// Adds some customer required ClassModel not directly binded to the
		// given UMLModel
		CollectionUtils.forAllDo(additionalClasses, ccm);


		// Generates the TableModels
		GenerateTablesClosure closure = new GenerateTablesClosure(umlArchive);
		CollectionUtils.forAllDo(ccm.getResult(), closure);
		// then writes the result out
		writeAll(closure);
	}

	private <E> void applyTemplate(Collection<E> models, CONTEXT contextName, StrBuilder sb, String templateName) {
		Iterator<E> iter = models.iterator();
		Object item = null;
		while (iter.hasNext()) {
			item = iter.next();
			if (item instanceof TableModel && ((TableModel) item).getAssociatedClass().equals("EARootClass"))
				continue;

			try {
				getVcontext().put(contextName.name(), item);
				sb.append("\t");
				sb.append(NmVelocity.getInstance().fillTemplate(getVcontext(), templateName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void writeAll(GenerateTablesClosure closure) throws IOException {
		PythonImportClosure importClosure = new PythonImportClosure();
		checkReciprocalTables(closure, importClosure);

		List<TableModel> tbs = sortTableModels(closure);
		// -- THINK BEFORE CHANGE THE ORDER!! --//
		applyTemplate(tbs, CONTEXT.tableModel, sqlTablesStrBuffer, tableTemplateFile);
		applyTemplate(closure.getNotSortable(), CONTEXT.tableModel, sqlTablesStrBuffer, tableTemplateFile);
		applyTemplate(closure.getAssociationTable(), CONTEXT.associationTable, sqlTablesStrBuffer,
				associationTemplateFile);
		vcontext.put(CONTEXT.imports.name(), importClosure.getImportMap());

		applyTemplate(tbs, CONTEXT.tableModel, sqlTablesStrBuffer, mapperTemplateFile);

		Set<TableModel> nsSimple = new HashSet<TableModel>();
		Iterator<TableModel> iter = closure.getNotSortable().iterator();
		while (iter.hasNext()) {
			TableModel tm = iter.next();
			if (tm.getInherited().size() == 0) {
				nsSimple.add(tm);
				iter.remove();
			}
		}

		applyTemplate(nsSimple, CONTEXT.tableModel, sqlTablesStrBuffer, notSortableMapperTemplateFile_1);
		applyTemplate(closure.getNotSortable(), CONTEXT.tableModel, sqlTablesStrBuffer, notSortableMapperTemplateFile_1);
		applyTemplate(nsSimple, CONTEXT.tableModel, sqlTablesStrBuffer, notSortableMapperTemplateFile_2);
		applyTemplate(closure.getNotSortable(), CONTEXT.tableModel, sqlTablesStrBuffer, notSortableMapperTemplateFile_2);
		// -------------------------------------//

		Writer sqlTablesWriter = new FileWriter(new File(getHelper().getBaseDir(), "sqlTables.py"), true);
		sqlTablesWriter
				.append("from sqlalchemy import Table, Column, ForeignKey, Sequence, event\n");
		sqlTablesWriter.append("from sqlalchemy.orm import relationship, mapper\n");			
		sqlTablesWriter.append("from sqlalchemy.orm.util import class_mapper\n");
		sqlTablesWriter.append("from sqlalchemy.dialects.postgresql import TEXT\n");			
		sqlTablesWriter.append("import inspect\n");		

		sqlTablesWriter.flush();

		// import
		// getVcontext().put(CONTEXT.imports.name(),
		// importCollector.getImportMap());
		sqlTablesWriter.append(NmVelocity.getInstance().fillTemplate(getVcontext(), importTemplateFile));
		sqlTablesWriter.append("\n\n");
		sqlTablesWriter.append("def next_id(connection, seq_name):\n");
		sqlTablesWriter.append("    seq = Sequence(seq_name)\n");
		sqlTablesWriter.append("    seq.create(bind=connection)\n");
		sqlTablesWriter.append("    return connection.execute(seq)\n");
		sqlTablesWriter.append("\n\n");
		sqlTablesWriter.append("def my_after_attach(session, instance):\n");
		sqlTablesWriter.append("    for item in inspect.getmembers(instance):\n");		
		sqlTablesWriter.append("    	if item[0] == 'synchronize':\n");
		sqlTablesWriter.append("    		instance.synchronize()\n");		
		sqlTablesWriter.append("\n\n");
		sqlTablesWriter.append("def attachEvents(session):\n");
		sqlTablesWriter.append("    event.listen(session, 'after_attach', my_after_attach)\n");
		sqlTablesWriter.append("\n\n");		
		sqlTablesWriter.append("def doTables(metadata):\n");		
		sqlTablesWriter.append(sqlTablesStrBuffer.toString());
		sqlTablesWriter.append("\n\n");		
		sqlTablesWriter.close();
	}

	private void checkReciprocalTables(GenerateTablesClosure closure, PythonImportClosure importClosure) {
		// Imports the classes necessaries for the table declaration
		CollectionUtils.forAllDo(closure.getTableModels(), importClosure);

		Iterator<TableModel> iterator = closure.getTableModels().iterator();
		while (iterator.hasNext()) {
			TableModel tb = iterator.next();
			/*
			 * Checks if one attribute or one of attribuite's children refers
			 * back to this TableModel
			 */
			checkTableLoop(tb, closure);

			/*
			 * Checks if one attribute of this TableModel 1) has the same name
			 * as one of TableModel's parents 2) has one of the TableModel's
			 * parent as parent too and eventually set up a many-to-many
			 * relationship
			 */
			checkTableAttributeOverriding(tb, importClosure);
		}
	}

	private void checkTableAttributeOverriding(TableModel tb, PythonImportClosure importClosure) {
		// Imports the classes necessaries for the table definition
		CollectionUtils.forAllDo(tb.getAttributeModel(), importClosure);

		Set<TableModel> ovm = new HashSet<TableModel>();
		for (AttributeModel am : tb.getAttributeModel()) {
			ovm.clear();
			for (TableModel inherited : tb.getInherited()) {
				for (AttributeModel inhAttribute : inherited.getAttributeModel()) {
					if (inhAttribute.getName().equals(am.getName())) {
						ovm.add(tb);
						ovm.add(inherited);
					}
				}
			}
			if (!ovm.isEmpty()) {
				tb.getOa().add(new OverriddenAttribute(ovm, am.getName()));
			}
		}
	}

	private void checkTableLoop(TableModel tb, GenerateTablesClosure closure) {
		Iterator<Relationship> iterator = tb.getRelationship().iterator();
		while (iterator.hasNext()) {
			Relationship rl = iterator.next();
			if (rl.isOneToOne())
				continue;
			LoopFinder lf = new LoopFinder(rl, closure.getTableModels().size(), closure);
			lf.search();
		}

//		Iterator<TableModel> tm = tb.getInherited().iterator();
//		while (tm.hasNext()) {
//			solveForeignLoop(tm.next(), tb, closure);
//		}
	}

	/**
	 * On the first pass the algorithm creates the inheritance relation in the
	 * form
	 * 
	 * // * --parent Column('id', primaryKey)
	 * 
	 * --child Column('parent_id', ForeignKey('parent.id'), primaryKey)
	 * 
	 * now if the child has an attribute[0..*] of the parent type
	 * 
	 * the structure is similar to
	 * 
	 * --parent Column('id', primaryKey) --parent Column('child_id',
	 * ForeignKey('child.id'))
	 * 
	 * 
	 * --child Column('parent_id', ForeignKey('parent.id'), primaryKey)
	 * --mapper(child, relationship(ParentType, 'child_field'))
	 * 
	 * which creates a circular reference
	 * 
	 * is necessary to modify the relation type to a many-to-many type and for
	 * this is necessary 1) to delete the foreignkey from the parent 2) modify
	 * the relation on the child 3) create a third (association) table
	 **/
	private void solveForeignLoop(TableModel parent, TableModel child, GenerateTablesClosure closure) {
		Iterator<ForeignKey> fk = parent.getFk().iterator();
		while (fk.hasNext()) {
			ForeignKey tmp_fk = fk.next();
			if (!tmp_fk.getTableModel().equals(child) || tmp_fk.getAssociatedRelationship().isOneToOne())
				continue;

			AssociationTable at = new AssociationTable(parent, child);
			if (!closure.getAssociationTable().contains(at)) {
				// create the third (association) table
				closure.getAssociationTable().add(at);
				updateRelationToManyToMany(child, parent);
				fk.remove();
				continue;
			}
		}

	}

	private class LoopFinder {
		private final int maxPathLength;
		private List<TableModel> oldPath = new ArrayList<TableModel>();
		private Set<Relationship> oldRelations = new HashSet<Relationship>();
		private final Relationship relationship;
		private final GenerateTablesClosure closure;

		public LoopFinder(Relationship relationship, List<TableModel> oldPath, int maxPathLength,
				GenerateTablesClosure closure, Set<Relationship> oldRelations) {
			super();
			this.relationship = relationship;
			this.maxPathLength = maxPathLength;
			this.oldPath.add(relationship.getFromTable());
			this.oldPath.add(relationship.getToTable());
			this.oldPath.addAll(oldPath);
			this.oldRelations.addAll(oldRelations);
			this.closure = closure;
		}

		public LoopFinder(Relationship relationship, int maxPathLength, GenerateTablesClosure closure) {
			this(relationship, new ArrayList<TableModel>(), maxPathLength, closure, new HashSet<Relationship>());
		}

		public void search() {
			if (logger.isDebugEnabled()) {
				logger.debug("oldRelations = " + oldRelations.size());
				logger.debug("relationship = " + relationship);
			}

			// if (oldRelations.contains(relationship))
			// return;
			// if (maxPathLength == oldPath.size())
			// return;
			if (relationship.getBackrefName() != null)
				return;
			if (relationship.isOneToOne())
				return;

			oldRelations.add(relationship);
			
			Set<Relationship> rels = new HashSet<Relationship>();
			rels.addAll(relationship.getToTable().getRelationship());
			Iterator<Relationship> iterator = rels.iterator();
			while (iterator.hasNext()) {
				Relationship item = iterator.next();
				if (!oldRelations.contains(item) && !item.isManyToMany()) {
					LoopFinder lf = new LoopFinder(item, oldPath, maxPathLength, closure, oldRelations);
					lf.search();
					continue;
				}
				
				if (relationship.isOneToMany()) {
					closure.transformToManyToMany(relationship);
				}
					
				if (item.isOneToMany()) {
					closure.transformToManyToMany(item);
				}	
				
				break;
			}
			oldRelations.remove(relationship);
		}

		/**
		 * On the first pass the algorithm has created one-to-many relation in
		 * the form
		 * 
		 * --parent mapper(parent, relationship(ChildType, 'parent_field'))
		 * 
		 * --child Column('parent_id', ForeignKey('parent.id'))
		 * 
		 * now if 1) the parent has an attribute of the child type 2) the child
		 * has an attribute of the parent type
		 * 
		 * the structure is similar to
		 * 
		 * --parent Column('child_id', ForeignKey('child.id')) mapper(parent,
		 * relationship(ChildType, 'parent_field'))
		 * 
		 * --child Column('parent_id', ForeignKey('parent.id')) mapper(child,
		 * relationship(ParentType, 'child_field'))
		 * 
		 * if there is a reciprocal one-to-many reference is necessary to modify
		 * the relation type to a many-to-many type and for this is necessary 1)
		 * to delete the foreignkey from both 2) delete the relation on the
		 * child 3) create a third (association) table 4) modify appropriately
		 * the parent relationship
		 **/
		private boolean checkReciprocalReference(Relationship parent) {
			// the relation is a self-reference			
			if (parent.getFromTable().equals(parent.getToTable())) {
				return false;
			}

			if (closure.getAssociationTable().contains(new AssociationTable(parent.getFromTable(), parent.getToTable()))
					|| closure.getAssociationTable().contains(new AssociationTable(parent.getToTable(), parent.getFromTable()))) {
				if (parent.isManyToOne() || parent.isOneToMany()) {
					if (logger.isWarnEnabled()) {
						logger.warn("Should update to ManyToMany relation: " + parent);
						//do something
					}
				}
				return false;
			}			
			return true;
		}
		
		/**
		 * The two <code>{@link Relationship}</code> refer directly one table to another. That is
		 * parent.fromTable == child.toTable
		 * child.fromTable == parent.toTable
		 */
		private void updateRelationToManyToMany(Relationship parentRl, Relationship childRl) {
			if (parentRl.isManyToMany() && childRl.isManyToMany()) {
				if (parentRl.getAssociationTable() == null  || childRl.getAssociationTable() == null) {
					if (logger.isErrorEnabled()) {
						logger.error(String.format("Relations are both ManyToMany but one or both have null AssociationTable. parentRl:%1$s  child:%2$s", parentRl, childRl));
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Both ManyToMany relations. parentRl:%1$s  child:%2$s", parentRl, childRl));
				}
			}
			
			if (parentRl.isOneToMany()) {
				closure.transformToManyToMany(parentRl);
			}
				
			if (childRl.isOneToMany()) {
				closure.transformToManyToMany(childRl);
			}
			
//			if (parentRl.isOneToMany() && childRl.isOneToMany()) {
//				transformToManyToMany(parentRl);
//				transformToManyToMany(childRl);
//			} else if (parentRl.isManyToMany() && childRl.isOneToMany()) {
//				// may be the parent has been already processed in another loop
//				// while the child still has been not
//				transformToManyToMany(childRl);
//			} else {
//				useAlterInFKFromTableModel(parentRl, childRl);
//			}
		}
		
		private void updateRelationToManyToMany(Relationship parentRl) {
			if (parentRl.isOneToMany()) {
				parentRl.setRelationType(RELATION_TYPE.MANY_TO_MANY);
				parentRl.getToTable().getFk().remove(parentRl.getAssociatedForeignKey());
				parentRl.setAssociatedForeignKey(null);
			}
		}
	}

	private void removeFKFromTables(Relationship onParent, Relationship onChild) {

		onChild.getToTable().getFk().remove(onChild.getAssociatedForeignKey());
		onParent.getToTable().getFk().remove(onParent.getAssociatedForeignKey());
		onParent.setAssociatedForeignKey(null);
		onChild.setAssociatedForeignKey(null);
		//removeFKFromTableModel(onParent.getFromTable(), onChild.getName(), onParent.getToTable());
		//removeFKFromTableModel(onParent.getToTable(), onParent.getName(), onParent.getFromTable());
	}

	private void useAlterInFKFromTableModel(Relationship onParent, Relationship onChild) {
		if (onParent.getAssociatedForeignKey() == null) {
			logger.error("Foreign key is missing in Relationship: " + onParent);
			return; //should throw exception
		}
		
		if (onParent.getAssociatedForeignKey() == null) {
			logger.error("Foreign key is missing in Relationship: " + onParent);
			return; //should throw exception
		}
		onParent.getAssociatedForeignKey().setUseAlter(true);
		onChild.getAssociatedForeignKey().setUseAlter(true);
		//useAlterInFKFromTableModel(onParent.getFromTable(), onChild.getName(), onParent.getToTable());
		//useAlterInFKFromTableModel(onParent.getToTable(), onParent.getName(), onParent.getFromTable());
	}

//	private void removeFKFromTableModel(TableModel model, String attrName, TableModel fromModel) {
//		Iterator<ForeignKey> fkchilditerator = model.getFk().iterator();
//		while (fkchilditerator.hasNext()) {
//			ForeignKey cfk = fkchilditerator.next();
//			if (cfk.getParentAttributeName().equals(attrName) && cfk.getTableModel().equals(fromModel)) {
//				fkchilditerator.remove();
//				return;
//			}
//		}
//	}

	private void useAlterInFKFromTableModel(TableModel model, String attrName, TableModel fromModel) {
		Iterator<ForeignKey> fkchilditerator = model.getFk().iterator();
		while (fkchilditerator.hasNext()) {
			ForeignKey cfk = fkchilditerator.next();
			if (cfk.getParentAttributeName().equals(attrName) && cfk.getTableModel().equals(fromModel)) {
				cfk.setUseAlter(true);
				return;
			}
		}
	}

	private void updateRelationToManyToMany(TableModel child, TableModel onParent) {
		for (Relationship rl : child.getRelationship()) {
			if (!rl.getToTable().equals(onParent))
				continue;

			rl.setRelationType(RELATION_TYPE.MANY_TO_MANY);
			return;
		}
	}

	private List<TableModel> sortTableModels(GenerateTablesClosure closure) {
		List<TableModel> finalTable = new ArrayList<TableModel>();
		Iterator<TableModel> iter = closure.getTableModels().iterator();
		TableModel tb = null;
		closure.getNotSortable().addAll(closure.getTableModels());
		while (iter.hasNext()) {
			tb = iter.next();
			if (tb.getRelationship().size() == 0 && tb.getInherited().size() == 0) {
				finalTable.add(tb);
				closure.getNotSortable().remove(tb);
			}
		}

		int index = -1;
		while (finalTable.size() < closure.getTableModels().size()) {
			index = finalTable.size();
			if (logger.isDebugEnabled())
				logger.debug("finalTable.size() : tableModels.size() - " + String.valueOf(finalTable.size()) + ":"
						+ String.valueOf(closure.getNotSortable().size()));
			updateFinalTable(finalTable, closure);

			// is possible more optimization?
			if (index == finalTable.size())
				break;
		}
		return finalTable;
	}

	private void updateFinalTable(List<TableModel> finalTable, GenerateTablesClosure closure) {
		List<TableModel> tm = new ArrayList<TableModel>(closure.getNotSortable());
		Collections.shuffle(tm);
		Iterator<TableModel> iter = tm.iterator();
		TableModel tb = null;
		while (iter.hasNext()) {
			tb = iter.next();
			if (finalTable.contains(tb))
				continue;

			boolean add = true;

			Iterator<Relationship> innerIter = tb.getRelationship().iterator();
			Relationship innerRelation = null;
			while (innerIter.hasNext()) {
				innerRelation = innerIter.next();
				if (innerRelation.getFromTable().equals(innerRelation.getToTable()))
					continue;
				add = finalTable.contains(innerRelation.getToTable()) && add;
			}

			Iterator<TableModel> inheritedIter = tb.getInherited().iterator();
			while (inheritedIter.hasNext()) {
				add = finalTable.contains(inheritedIter.next()) && add;
			}

			if (add) {
				finalTable.add(tb);
				closure.getNotSortable().remove(tb);
			}
		}
	}

	/**
	 * @return the vcontext
	 */
	private Context getVcontext() {
		return vcontext;
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
}
