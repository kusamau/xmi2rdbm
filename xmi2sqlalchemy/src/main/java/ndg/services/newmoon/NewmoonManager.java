package ndg.services.newmoon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.ArrayUtils;
import org.exist.xmldb.XQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.CompiledExpression;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

public class NewmoonManager {

	/**
	 * The system dependent representation of a line separator.
	 */
	public static final String LSEP = System.getProperty("line.separator");

	/**
	 * A standard XML 1.0 declaration using UTF-8 encoding.
	 */
	public static final String XML_DECLARATION_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	/**
	 * A suggested prefix value for the results document namespace.
	 */
	public static final String RESULTS_NS_SUGGESTED_PREFIX = "cr";

	/**
	 * The results document root element, local name value.
	 */
	public static final String RESULTS_ELEMENT_LOCAL_NAME = "results";

	/**
	 * The results document namespace.
	 */
	public static final String RESULTS_NS = "http://ndg.service.newmoon.conftest-result/1.0";

	enum MODULE_NAME {
		CONFORMANCE_TEST("conformance-test"), SCHEMA_ENCODING("schema-encoding");

		private final String name;

		private MODULE_NAME(String name) {
			this.name = name;
		}

		public String getCollectionName() {
			return "modules/" + this.name;
		}

		public String getModuleName() {
			return this.name;
		}
	}

	enum RULES {
		MODULES("modules"), PRE("pre"), POST("post"), TESTS("");

		private final String name;

		private RULES(String name) {
			this.name = name;
		}

		public String getRuleName() {
			return this.name;
		}
	}

	public enum NM_PARAM {
		XMI_DOC("xmi-path"), REPORT_NAME("reportname"), WORKING_DIRECTORY("working-directory"), DEPENDENCY_REGISTER_PATH(
				"dependency-register-path"), GLM_VERSION("gml-version"), ROOT_COLLECTION_URI("root-collection-uri"), XMLDB_USER(
				"xmldb-user"), XMLDB_PASSWORD("xmldb-password"), ROOT_COLLECTION_PATH("root-collection-path"), DEFAULT_ENCODE_REPORT_NAME(
				"EncodeReport"), DEFAULT_CONFORMANCE_REPORT_NAME("ConformanceTestReport"), CONFORMANCE_TEST_DIR(
				"conformanceDir"), ENCODE_DIR("encodeDir"), RESOURCE_NAME("defaultResourceName"), OUTPUT_DIR(
				"exportDir"), EXECUTION_DIR("execution-directory"), XMI_ADD_CLASSES("add-classes"), XMI_ADD_CONSTRAINTS("add-constraints");

		private final String name;

		private NM_PARAM(String name) {
			this.name = name;
		}

		public String getParamName() {
			return this.name;
		}
	}

	public enum GML_VERSION {
		v3_1("3.1"), v3_2("3.2");

		private final String name;

		private GML_VERSION(String name) {
			this.name = name;
		}

		public String getVersion() {
			return this.name;
		}
	}

	/**
	 * Name of the default output report name (with no posfix). Currently used
	 * as the basis for both .xml and .html output.
	 */
	public static final String DEFAULT_REPORT_NAME = "ConformanceTestReport";
	public static final String DEFAULT_ENCODE_REPORT_NAME = "EncodeReport";

	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(NewmoonManager.class);

	private static final Map<String, Object> variables = new HashMap<String, Object>();

	private Collection loadQueries(String moduleCollectionName, Collection aCollection, String queryDir)
			throws XMLDBException, IOException {
		Collection col = DBManager.getInstance().createCollection(aCollection, moduleCollectionName);
		addModule(col, queryDir);
		return col;
	}

	private void addModule(Collection col, String path) throws IOException, XMLDBException {
		List<File> modules = getPathChilds(path + "/modules");
		for (File file : modules) {
			DBManager.getInstance().addBinaryResource(col, file.getName(), file, "application/xquery");
		}
	}

	private List<File> getPathChilds(String path) throws IOException {
		File queries = new File(path);
		if (!queries.exists()) {
			return null;
		}

		return (List<File>) FileUtils.listFiles(queries,
				FileFilterUtils.notFileFilter(FileFilterUtils.directoryFileFilter()), null);
	}

	public void addXMIResource(Collection collection, File file, String resourceId, String collectionName)
			throws XMLDBException, IOException, NewmoonException {
		if (null == file) {
			throw new NewmoonException("The XMI resource file is NULL");
		}

		if (null == collectionName || collectionName.trim().length() == 0) {
			throw new NewmoonException("The XMI resource file collectionName is NULL or empty");
		}

		Collection xmiCol = DBManager.getInstance().createCollection(collection, collectionName);
		xmiCol.setProperty("encoding", "UTF-8");
		DBManager.getInstance().addXMLResource(xmiCol, resourceId, file);
	}

	protected void setVariables(Map<NM_PARAM, String> params) {
		for (NM_PARAM param : params.keySet()) {
			variables.put(param.getParamName(), params.get(param));
		}

		initVariablesMap();
	}

	protected void setVariables(NM_PARAM param, String value) {
		variables.put(param.getParamName(), value);
	}

	protected String getVariables(NM_PARAM param) {
		return (String) variables.get(param.getParamName());
	}

	private void initVariablesMap() {
		variables.put(NM_PARAM.OUTPUT_DIR.getParamName(),
				FilenameUtils.getBaseName(FilenameUtils.getPathNoEndSeparator((String) variables.get(NM_PARAM.XMI_DOC.getParamName()))) + File.separator
						+ NM_PARAM.OUTPUT_DIR.getParamName());
		String rootCollectionPath = (String)variables.get(NM_PARAM.ROOT_COLLECTION_URI.getParamName());
		rootCollectionPath = rootCollectionPath.replaceAll("^(.*/exist/xmlrpc)(/[^/]*)$", "$2");
		variables.put(NM_PARAM.ROOT_COLLECTION_PATH.getParamName(), rootCollectionPath);
	}

	/**
	 * Runs the suite of pre- and post-processing XQueries.
	 * 
	 * @throws IOException
	 * @throws XMLDBException 
	 */
	protected List<Resource> executePreProcessingScripts(Resource xmiDoc, RULES rules, String xqueryDir)
			throws IOException, XMLDBException {

		List<Resource> results = new ArrayList<Resource>();
		List<File> queryTests = null;

		if (rules.getRuleName().trim().length() > 0) {
			queryTests = getPathChilds(xqueryDir + "/" + rules.getRuleName());
		} else {
			queryTests = getPathChilds(xqueryDir);
		}

		if (queryTests == null || queryTests.size() == 0) {
			return results;
		}

		Collections.sort(queryTests, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

		for (File file : queryTests) {
			if (!FilenameUtils.wildcardMatch(file.getName(), "*.xq")) {
				continue;
			}

			// Add the results to the ConformaceTestResults bean.
			ResourceSet result;
			Resource rs = null;
			result = executeXQuery(xmiDoc, file, variables);
			ResourceIterator rIter = result.getIterator();
			while (rIter.hasMoreResources()) {
				rs = rIter.nextResource();
				results.add(rs);	
			}
		}
		return results;
	}

	/**
	 * Executes an XPath or XQuery statement on the database and returns the
	 * results of the query as a <code>ResourceSet</code> object.
	 * 
	 * @param query
	 *            the query string - an XPath or XQuery string.
	 * 
	 * @param externalVariables
	 *            an optional HashMap of string key/value pairs. Can be null if
	 *            the query declares no external variables.
	 * 
	 * @return the results of the query as a <code>ResourceSet</code> object.
	 * 
	 * @throws XMLDBException
	 *             if there is an error executing the query.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private ResourceSet executeXQuery(Resource resource, File query, Map<String, Object> externalVariables)
			throws XMLDBException, FileNotFoundException, IOException {
		XQueryService service = DBManager.getInstance().getXQueryService();
		if (null != externalVariables) {
			Iterator<String> keys = externalVariables.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = externalVariables.get(key);
				service.declareVariable(key, value);
			}
		}
		CompiledExpression compiled = service.compile(IOUtils.toString(new FileReader(query)));
		return service.execute((XMLResource) resource, compiled);
	}

	public InputStream precessResource(Resource xmiResource, String resourceID, MODULE_NAME collectionName,
			String xqueryDir, File outputDir) throws XMLDBException, IOException {
		InputStream ret = null;
		if (xqueryDir == null) {
			return null;
		}
		String moduleCollectionName = collectionName.getCollectionName() + "/" + FilenameUtils.getName(xqueryDir);
		loadQueries(moduleCollectionName, null, xqueryDir);

		List<Resource> results = new ArrayList<Resource>();
		List<Resource> pre = executePreProcessingScripts(xmiResource, RULES.PRE, xqueryDir);
		results.addAll(pre);

		DBManager.getInstance().mapDBCollection(null);
		List<Resource> tests = executePreProcessingScripts(xmiResource, RULES.TESTS, xqueryDir);
		results.addAll(tests);

		List<Resource> post = executePreProcessingScripts(xmiResource, RULES.POST, xqueryDir);
		results.addAll(post);
			
		//This has to be fixed
		String defaultReportStylesheet = "http://ndg.services.newmoon/results-stylesheet.xsl";
		ret = reportToFile(resourceID, defaultReportStylesheet, results, (String) variables.get(NM_PARAM.REPORT_NAME.getParamName()), outputDir);
		return ret;
	}

	/**
	 * Serialises the current state of the results collection to an XML document
	 * representation.
	 * 
	 * @return an XML representation of the current state of the results
	 *         collection.
	 * @throws XMLDBException
	 * @throws IOException
	 */
	public InputStream reportToFile(String resourceID, String stylesheet, List<Resource> results, String reportName, File outputDir)
			throws XMLDBException, IOException {
		final String LS = LSEP;
		StringBuffer buffy = new StringBuffer(XML_DECLARATION_STRING).append(LS);
		buffy.append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(stylesheet).append("\"?>").append(LS);
		buffy.append("<").append(RESULTS_NS_SUGGESTED_PREFIX).append(":");
		buffy.append(RESULTS_ELEMENT_LOCAL_NAME).append(" xmlns:").append(RESULTS_NS_SUGGESTED_PREFIX);
		buffy.append("=\"").append(RESULTS_NS).append("\"");
		if (resourceID != null)
			buffy.append(" resource=\"" + resourceID + "\"");
		buffy.append(">").append(LS);

		for (Resource result : results) {
			try {
				buffy.append((String) result.getContent()).append(LS);
			} catch (Exception e) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("possible error", e);	
				}
			}
		}
		// Append each result to the root element.
		int resultCount = results.size();
		for (int i = 0; i < resultCount; i++) {

		}
		buffy.append("</").append(RESULTS_NS_SUGGESTED_PREFIX).append(":");
		buffy.append(RESULTS_ELEMENT_LOCAL_NAME).append(">").append(LS);

		// Write out the raw XML report.
		File fXmlReport = new File(outputDir, reportName + ".xml");
		FileWriter writer = new FileWriter(fXmlReport);
		writer.write(buffy.toString());
		writer.flush();
		writer.close();
		return (InputStream) new FileInputStream(fXmlReport);
	}

	public List<String> exportGeneratedResource(String collectionPath, File exportDir) throws NewmoonException,
			XMLDBException {
		List<String> encodeFilePaths = new ArrayList<String>();
		Collection collection = DBManager.getInstance().getCollection(null, collectionPath);
		String[] collectionResources = collection.listResources();
		if (ArrayUtils.isEmpty(collectionResources)) {
			LOG.info("No resources to export");
			return encodeFilePaths;
		}
		for (String item : collectionResources) {
			Resource resource = DBManager.getInstance().getResource(collection, item);
			String docString = null;
			try {
				docString = new String(resource.getContent().toString().getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new NewmoonException(e);
			}
			docString = postGenerationCleanUp(docString);

			// Write the contents to a file.
			FileWriter writer = null;
			File tmpFile = null;
			try {
				tmpFile = new File(exportDir, item);
				writer = new FileWriter(tmpFile);
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				writer.write(docString);
				writer.flush();
				writer.close();
				if (LOG.isInfoEnabled()) {
					LOG.info("Saved " + item + " to directory " + tmpFile.getPath());
				}
				encodeFilePaths.add(tmpFile.getAbsolutePath());
			} catch (IOException e) {
				throw new NewmoonException(e);
			}
		}
		return encodeFilePaths;
	}

	/**
	 * Perform post-generation clean-up procedure on XDS documents.
	 */
	private String postGenerationCleanUp(String xsdDoc) {
		Pattern p = Pattern.compile("\\s+\\S+:dummy=\"\"");
		Matcher m = p.matcher(xsdDoc);
		return m.replaceAll("");
	}
}