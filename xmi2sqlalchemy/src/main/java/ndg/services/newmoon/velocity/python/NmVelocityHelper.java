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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ndg.services.newmoon.NmParserHelper;
import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.UMLElementsArchive.PropertyMap;
import ndg.services.newmoon.collect.AttributeModel;
import ndg.services.newmoon.collect.ClassModel;
import ndg.services.newmoon.xmiModel.UMLClassDataType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Contains static methods used by the templating files
 * @author mnagni
 *  
 */
public class NmVelocityHelper {

	private static Properties xmiToSQLMaps = null;
	
	private static final Set<String> pythonKeywordSet = new HashSet<String>();
	
	private static final String pythonKeyword = "and, del, from, not, while, " +
			"as, elif, global, or, with," +
			"assert, else, if, pass, yield," +
			"break, except, import, print," +
			"class, exec, in, raise," +
			"continue, finally, is, return," +
			"def, for, lambda, try";
	
	/**
	 * Checks is a given string is a python keyword  
	 */
	public static String checkName(String name) {
		if (CollectionUtils.isEmpty(pythonKeywordSet)) {
			String[] keys = StringUtils.split(pythonKeyword, ",");
			keys = StringUtils.stripAll(keys);
			CollectionUtils.addAll(pythonKeywordSet, keys);
		}
		return pythonKeywordSet.contains(name) ? name + "_" : name;
	}
	
	public static boolean isValidPythonName(String text) {
		return StringUtils.containsNone(text, "[]<>.,*/|\\@:;+-#!%^£$()");
	} 
	
	public static boolean validateClass(ClassModel classModel) {
		for (ClassModel cm : classModel.getParents()) {
			if (!NmVelocityHelper.isValidPythonName(cm.getAssociatedClass().getName())) {
				return false;
			}
		}
		return NmVelocityHelper.isValidPythonName(classModel.getAssociatedClass().getName());
	}	
	
	/**
	 * Checks if the given class name is mapped by {@link PropertyMap.XMI_TO_SQL_MAP}
	 * @param className a class name
	 * @return <code>true</code> if the internal map has a key with such class name, 
	 * <code>false</code> otherwise 
	 **/
	public static boolean isExternallyMapped(String className) {
		return getXMIToSQL().containsKey(className);
	}

	public static String limitLenght(String name) {
		int end = name.length() < 63 ? name.length() : 62;
		return name.substring(0, end);
	}	
	
	/**
	 * Returns an "almost" ordered list in order to avoid to the python MRO
	 * conflicts during a python class initialization. 
	 * For example if B is parent of A the following python class definition 
	 * is an error
	 * class C(A,B)
	 *   pass
	 * 
	 * while
	 * 
	 * class C(B,A)
	 *   pass
	 * 
	 * is correct. 
	 **/
	public static List<ClassModel> getOrderedParents(ClassModel classModel) {
		Set<ClassModel> set_cms = new HashSet<ClassModel>();
		set_cms.addAll(classModel.getParents());
		
		List<ClassModel> cms = new ArrayList<ClassModel>();
		cms.addAll(set_cms);
		if (cms.size() > 1) {
			Collections.sort(cms);	
		}		
		return cms;
	}	
	
	public static String getPythonModule(UMLClassDataType umlClass) {
		return NmParserHelper.getPackageParentName(umlClass.getUmlPackage()) + "." + umlClass.getName().toLowerCase();
	}	

	/**
	 * Returns an array containg on the first element the name of the module from
	 * where import and on the second the name of which class import. This methods
	 * takes care to substitute when necesary the  
	 * @param className a class name
	 * @return a two elements array, <code>null</code> if {@link #isExternallyMapped(String)} returns <code>false</code> 
	 * */
	public static String[] formatPythonImportString(UMLClassDataType umlClass) {
		String[] ret = null;
		if (umlClass == null) {
			return ret;
		}
		if (isExternallyMapped(umlClass.getName())) {
			ret = new String[2];
			String clazz = getXMIToSQL().getProperty(umlClass.getName());
			ret[0] = StringUtils.substringBeforeLast(clazz, ".");
			ret[1] = StringUtils.substringAfterLast(clazz, ".");
		} else {
			ret = new String[2];
			ret[0] = NmParserHelper.getPackageParentName(umlClass.getUmlPackage()) + "." + umlClass.getName().toLowerCase();
			ret[1] = umlClass.getName();
		}
		return ret;
	}
	
	private static String formatPythonImportString(UMLClassDataType umlClass, String type) {
		String[] ret = formatPythonImportString(umlClass);
		if (ret != null) {
			if (type.equals("Module")) {
				return ret[0];	
			} else if (type.equals("Class")) {
				return ret[1];
			}			
		} 
		return ""; 
	} 
	
	/**
	 * Returns the module from where import a given class. Is a shortway to get 
	 * the first element of {@link #formatPythonImportString(UMLClassDataType)}
	 * @param className a class name
	 * @return the module name, else return the given className 
	 **/
	public static String getMappedModule(UMLClassDataType umlClass) {
		return formatPythonImportString(umlClass, "Module");
	}	

	/**
	 * Returns the Class from where import a given class. Is a shortway to get 
	 * the first element of {@link #formatPythonImportString(UMLClassDataType)}
	 * @param className a class name
	 * @return the module name, else return the given className 
	 **/
	public static String getMappedClass(UMLClassDataType umlClass) {
		return formatPythonImportString(umlClass, "Class");
	}
	
	/**
	 * Transforms a name in a python lexically correct form.
	 * This is done replacing the illegal character with an underscore ('_'). 
	 * Characted case is not changed. The list of replacements is
	 * 
	 * <ul>
	 * <li>space</li>
	 * <li>-</li>
	 * <li>(</li>
	 * <li>)</li> 
	 * </ul>
	 * @param text the name to transform
	 * @return the tranformed name
	 **/
	public static String transformToPythonName(String text) {
		String trans = StringUtils.replaceChars(text, '-', '_').toLowerCase();
		 trans = StringUtils.replaceChars(trans, '/', '_').toLowerCase();
		 trans = StringUtils.replaceChars(trans, ')', '_').toLowerCase();
		 trans = StringUtils.replaceChars(trans, '(', '_').toLowerCase();
		 trans = StringUtils.replaceChars(trans, '.', '_').toLowerCase();
		return StringUtils.replaceChars(trans, ' ', '_');
	}	
	
	public static String substituteDBType(String text) {
		String clazz = getXMIToSQL().getProperty(text);
		if (clazz == null) {
			return text;
		}
		return StringUtils.substringAfterLast(clazz, ".");
	}		
	
	public static String substituteDBType(AttributeModel attribute) {
		if (attribute == null 
				|| attribute.getAssociatedType() == null 
				|| attribute.getAssociatedType().getAssociatedClass() == null) {
			System.out.println("Null Substitute type!");
			return "";
		}
		String ret = attribute.getAssociatedType().getAssociatedClass().getName();
		String clazz = substituteDBType(ret);
		
		// From now I start to condider the codelist as string because
		// the UML definition is too much bind to the customer taste,
		// (group of two or more codelists, use of URL) so in not possible
		// to define a static list
		if (attribute.getAssociatedType().isCodeList()) {
			clazz = substituteDBType("CodeList");
			if (!clazz.equals("CodeList")) {
				return clazz;
			}			
		}
		
		if (attribute.getAssociatedType().isCodeList() || attribute.getAssociatedType().isEnumeration()) {
			// The IF below has to be fixed
			// 1) is not admissible the equal(BOOLEAN) check
			// 2) has to be created a custom type to handle the ARRAY(ENUM) case
			//has to be fixed! cannot use the "IF" below!!
			if (!clazz.equals("BOOLEAN")) {
				clazz += ".db_type()";
				return clazz;
			}			
			
			if (!clazz.equals("BOOLEAN")) {
				clazz += ".db_type()";
				return clazz;
			}
		}
		
		//Actually I cannot manage type like Sequence<Vector> and for this reason 
		// I set them by default to ARRAY<TEXT>
		if (clazz.equalsIgnoreCase(substituteDBType("Vector"))){
			return "ARRAY(" + substituteDBType("CharacterString") + ")";
		}
		
		if (attribute.hasMultiplicity()) {
				return "ARRAY(" + clazz + ")";	
		}
		return clazz;
	}	
	
	private static Properties getXMIToSQL() {
		if (xmiToSQLMaps == null)
			xmiToSQLMaps = UMLElementsArchive.loadProperties(PropertyMap.XMI_TO_SQL_MAP);
		return xmiToSQLMaps;		
	}
	
	public static void showAttribute(AttributeModel attribute) {
		AttributeModel am = attribute;
	}
}
