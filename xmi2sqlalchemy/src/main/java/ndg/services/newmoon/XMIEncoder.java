package ndg.services.newmoon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import ndg.common.exception.ResourceNotAvailable;
import ndg.services.newmoon.NewmoonManager.MODULE_NAME;
import ndg.services.newmoon.NewmoonManager.NM_PARAM;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;

public class XMIEncoder {

	private String outputdirPath = null; 
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(XMIEncoder.class);
	
	public String getOutputdirPath() {
		return outputdirPath;
	}

	public void doEncode(Map<NM_PARAM, String> params)
			throws NewmoonException {
		try {
			executeEncode(params);
		} catch (XMLDBException e) {
			throw new NewmoonException(e);
		} catch (IOException e) {
			throw new NewmoonException(e);
		}
	}

	private void updateDefaultRegister(String workingDirectory) {
        URL oracle;
        File defaultRegister = new File(workingDirectory, "dependency/Register_ExternalPackages.xml");
        File originalDefaultRegister = new File(workingDirectory, "dependency/Register_ExternalPackagesOriginal.xml");
		try {
			oracle = new URL("http://projects.arcs.org.au/trac/fullmoon/browser/trunk/resources/dependency/Register_ExternalPackages.xml?format=txt");
	        URLConnection yc = oracle.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        String inputLine;
	        FileWriter writer = new FileWriter(defaultRegister);
	        while ((inputLine = in.readLine()) != null) 
	        	writer.write(inputLine);
	        writer.flush();
	        writer.close();
	        in.close();
		} catch (Exception e) {
			LOG.info("Cannot update the defaultRegister. Will use the deployed original", e);
			applyOriginalDefaultRegister(defaultRegister, originalDefaultRegister);
		}
	}
	
	private void applyOriginalDefaultRegister(File defaultRegister, File originalDefaultRegister) {
		FileInputStream input;
		try {
			input = new FileInputStream(originalDefaultRegister);
			FileOutputStream output = new FileOutputStream(defaultRegister);
			IOUtils.copy(input, output);
		} catch (Exception e) {
			LOG.info("Cannot retrieve Register file", e);
		}
	}
	
	private void executeEncode(Map<NM_PARAM, String> params)
			throws XMLDBException, IOException, NewmoonException {
		NewmoonManager nm = new NewmoonManager();
		nm.setVariables(params);
		updateDefaultRegister(nm.getVariables(NM_PARAM.WORKING_DIRECTORY));
		//Prepare the outputDir
		File outputDir = new File(nm.getVariables(NM_PARAM.EXECUTION_DIR), nm.getVariables(NM_PARAM.OUTPUT_DIR));
		outputDir.mkdir();
		outputdirPath = outputDir.getPath();
		
		DBManager.setDBAccess(nm.getVariables(NM_PARAM.XMLDB_USER),
				nm.getVariables(NM_PARAM.XMLDB_PASSWORD), 
				nm.getVariables(NM_PARAM.ROOT_COLLECTION_URI));
		
		File xmiDoc = new File(nm.getVariables(NM_PARAM.EXECUTION_DIR), nm.getVariables(NM_PARAM.XMI_DOC));
		
		String resourceName = nm.getVariables(NM_PARAM.RESOURCE_NAME);		
		if (resourceName == null) {
			resourceName = NM_PARAM.RESOURCE_NAME.getParamName();
		}
		
		String schemaEncoding = FilenameUtils.getName(nm.getVariables(NM_PARAM.ENCODE_DIR));
		nm.addXMIResource(null, xmiDoc, resourceName, schemaEncoding);
		Collection xmiCol = DBManager.getInstance().getCollectionChild(null, schemaEncoding);
		Resource xmiResource = DBManager.getInstance().getResource(xmiCol, resourceName);
		InputStream is = null;
		
		// Test the XMI File		
		if (!params.containsKey(NM_PARAM.DEFAULT_CONFORMANCE_REPORT_NAME)) {
			nm.setVariables(NM_PARAM.REPORT_NAME, NM_PARAM.DEFAULT_CONFORMANCE_REPORT_NAME.getParamName());
		} else {
			nm.setVariables(NM_PARAM.REPORT_NAME, params.get(NM_PARAM.DEFAULT_CONFORMANCE_REPORT_NAME));
		}
		
		is = nm.precessResource(xmiResource, resourceName, MODULE_NAME.CONFORMANCE_TEST,
				nm.getVariables(NM_PARAM.CONFORMANCE_TEST_DIR), outputDir);
		testReport(is);

		// Encode the XMI File
		if (!params.containsKey(NM_PARAM.DEFAULT_ENCODE_REPORT_NAME)) {
			nm.setVariables(NM_PARAM.REPORT_NAME, NM_PARAM.DEFAULT_ENCODE_REPORT_NAME.getParamName());
		} else {
			nm.setVariables(NM_PARAM.REPORT_NAME, params.get(NM_PARAM.DEFAULT_ENCODE_REPORT_NAME));
		}
		is = nm.precessResource(xmiResource, resourceName, MODULE_NAME.SCHEMA_ENCODING,
				nm.getVariables(NM_PARAM.ENCODE_DIR), outputDir);
		
		DBManager.getInstance().mapDBCollection(null);
		nm.exportGeneratedResource(MODULE_NAME.SCHEMA_ENCODING.getModuleName() + "/" + schemaEncoding + "/working", outputDir);
	}

	private void testReport(InputStream is) throws NewmoonException {
		ConformanceTestReportManager ctr = new ConformanceTestReportManager();
		List<ReportError> errs = null;
		StrBuilder sb = new StrBuilder();
		if (is != null) {
			try {
				errs = ctr.scanForErrors(is);
				if (errs != null && !errs.isEmpty()) {
					for (ReportError err : errs) {
						sb.appendln(err.toString());
					}
					throw new NewmoonException(sb.toString());
				}
			} catch (ResourceNotAvailable e) {
				LOG.error("Error!", e);
			} catch (JAXBException e) {
				LOG.error("Error!", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					LOG.error("Error!", e);
				}
				//is = null;
			}
		}
	}
}