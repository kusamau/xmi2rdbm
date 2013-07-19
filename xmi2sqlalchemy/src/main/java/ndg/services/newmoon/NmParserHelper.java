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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import ndg.services.newmoon.xmiModel.UMLModel;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author mnagni
 *
 */
public class NmParserHelper {

	//private final InputSource inputSource;

	final static Logger logger = LoggerFactory.getLogger(NmParserHelper.class);	
	
	public NmParserHelper() {
		super();
	}
	
	/**
	 * Returns the package name expression of the given UMLPackage.
	 * The format is in the typical Python/Java form ('.' separated)
	 * */
	public static String getPackageParentName(UMLModel umlPackage) {
		if (umlPackage == null) return null;
		
		StringBuffer sb = new StringBuffer();
		sb.insert(0, convertForbiddenChars(umlPackage.getName()));	
		
		UMLModel parent = umlPackage.getParent();
		while (parent != null) {
			sb.insert(0, '.');
			sb.insert(0, convertForbiddenChars(parent.getName()));	
			parent = parent.getParent();
		}
		return sb.toString();
	}
	
	private static String convertForbiddenChars(String text) {
		String newText  = StringUtils.replaceChars(text.trim(), '.', '_');
		newText  = StringUtils.replaceChars(newText, '-', '_');
		newText  = StringUtils.replaceChars(newText, ' ', '_');
		newText  = StringUtils.replaceChars(newText, ':', '_');
		newText  = StringUtils.replaceChars(newText, '/', '_');		
		newText = StringUtils.remove(newText, '(');
		newText = StringUtils.remove(newText, ')');
		return newText.toLowerCase();
	}
	
	//prevent the parser to return a simple text node (can happen more often than thought...)
	public Node getFirstSiblingNoTextNode(Node node) {
		if (node == null)
			return null;
				
		if (node.getNodeType() == Node.TEXT_NODE) {
			node = getFirstSiblingNoTextNode(node.getNextSibling());
		}		
		return node;
	}
	
	public static Set<String> getCSVFromInputStream(InputStream is) {
		Set<String> ret = new HashSet<String>();
		try {
			String isText = IOUtils.toString(is);
			String[] ss = StringUtils.split(isText, ",");
			ret = new HashSet<String>(Arrays.asList(StringUtils.stripAll(ss)));
		} catch (IOException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Cannot read the Additional Clases file");	
			}
		}
		return ret;
	}
}
