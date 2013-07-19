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
package ndg.services.newmoon.gml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

import ndg.common.exception.ResourceNotAvailable;
import ndg.services.newmoon.NewmoonException;
import ndg.services.newmoon.NewmoonManager;
import ndg.services.newmoon.XMIParserAbstract;
import ndg.services.newmoon.exception.NM_ParamException;
import net.sf.saxon.xqj.SaxonXQDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author mnagni
 * 
 */
public class ValidateXMIModel  extends XMIParserAbstract {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ValidateXMIModel.class);

	private final DocumentHelper dh = new DocumentHelper();
	private final XQDataSource xQDataSource;
	private XQConnection conn = null;

	public ValidateXMIModel() {
		super();
		this.xQDataSource = new SaxonXQDataSource();
	}

	public void execute(Map<NewmoonManager.NM_PARAM, String> params) throws NewmoonException {
		File baseDir = null;
		InputStream inputXML = null;
		try {
			baseDir = new File(getParam(params, NewmoonManager.NM_PARAM.EXECUTION_DIR));
			inputXML = openInputStream(params, NewmoonManager.NM_PARAM.XMI_DOC, baseDir);		
		} catch (NM_ParamException ex) {
			throw new NewmoonException("Not valid parameter", ex);
		}
		try {
			doTest(inputXML);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotAvailable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private final void doTest(final InputStream inputXML) throws XPathExpressionException, XQException,
			ParserConfigurationException, SAXException, IOException, ResourceNotAvailable {
		Document doc = dh.parseDocument(inputXML);		
		XQExpression xqe = getXQConnection().createExpression();
		xqe.bindNode(XQConstants.CONTEXT_ITEM, doc, null);
		executeQuery(xqe);
	}

	private XQConnection getXQConnection() throws XQException {
		if (conn == null) {
			conn = xQDataSource.getConnection();
			// create the ItemTypes for string and integer
			//strType = conn.createAtomicType(XQItemType.XQBASETYPE_STRING);
			//intType = conn.createAtomicType(XQItemType.XQBASETYPE_INT);
		}
		return conn;
	}

	private XQResultSequence executeQuery(final XQExpression xqe) {
		XQResultSequence res = null;		
		try {
			InputStream xquery = ValidateXMIModel.class.getClassLoader().getResourceAsStream("gmlRules/test-001.xq");
			res = xqe.executeQuery(xquery);
		} catch (XQException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Error! But continuing to loop. Detailed error follows ", e);
			}
		}
		return res;
	}
}
