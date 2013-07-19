package ndg.services.newmoon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.exist.xmldb.EXistResource;
import org.exist.xmldb.XQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;


public class DBManager {

	private static String user;
	private static String psw;
	private static String rootCollectionPath;
	private static boolean initialized = false;

	private final static DBManager instance = new DBManager();

	private final static void initialize() {
		String driver = "org.exist.xmldb.DatabaseImpl";		
		// initialize database driver		
		try {
			Class<?> cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			DatabaseManager.registerDatabase(database);
		} catch (ClassNotFoundException e) {
			LOG.error("Error", e);
		} catch (InstantiationException e) {
			LOG.error("Error", e);
		} catch (IllegalAccessException e) {
			LOG.error("Error", e);
		} catch (XMLDBException e) {
			LOG.error("Error", e);
		}
		initialized = true;
	}
	
	/**
	 * User name for database access.
	 */
	// static final String XMLDB_USER = "admin";

	/**
	 * Password for the XMLDB_USER account.
	 */
	// static final String XMLDB_PASSWORD = "newmoon";

	/**
	 * Identification string for the CollectionManagementService XML:DB service.
	 */
	private static final String COLLECTION_MGMT_SERVICE_NAME = "CollectionManagementService";

	/**
	 * Identification string for the CollectionManagementService XML:DB service
	 * version.
	 */
	private static final String COLLECTION_MGMT_SERVICE_VERSION = "1.0";

	/**
	 * Identification string for the XQueryService XML:DB service.
	 */
	private static final String XQUERY_SERVICE_SERVICE_NAME = "XQueryService";

	/**
	 * Identification string for the XQueryService XML:DB service version.
	 */
	private static final String XQUERY_SERVICE_SERVICE_VERSION = "1.0";

	/**
	 * Property name to apply indentation on XML:DB XQueryService result sets.
	 */
	public static final String INDENT_PROPERTY_NAME = "indent";

	/**
	 * Identification string for the DatabaseInstanceManager XML:DB service.
	 */
	public static final String USER_MGMT_SERVICE_NAME = "UserManagementService";

	/**
	 * Identification string for the DatabaseInstanceManager XML:DB service
	 * version.
	 */
	public static final String USER_MGMT_SERVICE_VERSION = "1.0";

	/**
	 * The literal "yes" String value.
	 */
	private static final String YES = "yes";

	/**
	 * The literal "no" String value.
	 */
	@SuppressWarnings("unused")
	private static final String NO = "no";

	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DBManager.class);

	/** Hides the default constructor */
	private DBManager() {
	};

	public static DBManager getInstance() {
		return instance;
	}

	public static void setDBAccess(String newUser, String newPwd, String newRootCollectionPath) {
		if (StringUtils.isEmpty(newUser) || StringUtils.isEmpty(newPwd) || StringUtils.isEmpty(newRootCollectionPath))
			return;
		user = newUser;
		psw = newPwd;
		rootCollectionPath = newRootCollectionPath;
		isInitialized();
		try {
			initialiseDBase();
		} catch (XMLDBException e) {
			LOG.error("Error", e);
		}
	}

	private static boolean isInitialized() {
		if (!initialized) {
			initialize();
		}			
		return initialized;
	}
	
	/*
	public static String getROOT_COLLECTION_PATH() {
		return "xmldb:exist://localhost:8080/exist/xmlrpc/db";
	}
	*/

	/**
	 * Creates a collection as an immediate child of the root collection.
	 * (providing that collection does not already exist.
	 * 
	 * @param collection
	 *            the name of the collection to create.
	 * 
	 * @throws XMLDBException
	 *             if there is an error creating the collection.
	 */
	public Collection createCollection(Collection collection, String newCollection) throws XMLDBException {
		collection = collection == null ? getRootCollection() : collection;
		CollectionManagementService mgtService = (CollectionManagementService) collection.getService(
				COLLECTION_MGMT_SERVICE_NAME, COLLECTION_MGMT_SERVICE_VERSION);
		Collection newCol = mgtService.createCollection(newCollection);
		LOG.debug("Created collection " + newCol.getName());
		return newCol;
	}

	public Resource getResource(Collection collection, String resourceID) throws XMLDBException {
		collection = collection == null ? getRootCollection() : collection;
		return collection.getResource(resourceID);
	}

	public String[] getCollectionResourcesList(String parentCollection, String collectionName) throws XMLDBException {
		String[] ret = null;
		Collection collection = getCollection(parentCollection, collectionName);
		if (collection != null) {
			ret = collection.listResources();
		}
		return ret;
	}

	public String[] getCollectionResourcesList(Collection collection) throws XMLDBException {
		collection = collection == null ? getRootCollection() : collection;
		return collection.listResources();
	}

	public Collection getCollection(String parentCollection, String collectionName) throws XMLDBException {
		Collection parent = getCollection(parentCollection);
		String[] children = collectionName.split("/");
		if (ArrayUtils.isEmpty(children)) {
			return parent;
		}
		Collection childColl = parent.getChildCollection(children[0]);
		for (int index = 1; index < children.length; index++) {
			childColl = childColl.getChildCollection(children[index]);
		}
		return childColl;
	}

	private Collection getCollection(String collectionName) throws XMLDBException {
		if(!isInitialized()) {
			throw new XMLDBException();
		} 
		collectionName = StringUtils.isBlank(collectionName) ? DBManager.rootCollectionPath : collectionName;
		return DatabaseManager.getCollection(collectionName, DBManager.user, DBManager.psw);
	}

	public void addXMLResource(Collection collection, String resourceId, File file) throws FileNotFoundException,
			XMLDBException, IOException {
		addXMLResource(collection, resourceId, IOUtils.toString(new FileReader(file)));
	}

	public void addXMLResource(Collection collection, String resourceId, String resourceXML)
			throws FileNotFoundException, XMLDBException, IOException {
		XMLResource resource = (XMLResource) collection.createResource(resourceId, "XMLResource");
		addResource(collection, resource, resourceXML);
	}

	public void addBinaryResource(Collection collection, String resourceId, File file, String mimeType)
			throws FileNotFoundException, XMLDBException, IOException {
		addBinaryResource(collection, resourceId, IOUtils.toString(new FileReader(file)), mimeType);
	}

	public void addBinaryResource(Collection collection, String resourceId, String resourceXML, String mimeType)
			throws FileNotFoundException, XMLDBException, IOException {
		BinaryResource resource = (BinaryResource) collection.createResource(resourceId, "BinaryResource");
		try {
			// eXist-specific code to do modification of mime-type
			((EXistResource) resource).setMimeType(mimeType);
		} catch (Throwable t) {
			// Ignore. The implication is that the binary resouce may
			// be unusable as an Xquery module due to incorrect MIME type.
		}
		addResource(collection, resource, resourceXML);
	}

	private void addResource(Collection collection, Resource resource, String resourceXML)
			throws FileNotFoundException, XMLDBException, IOException {
		resource.setContent(resourceXML);
		collection.storeResource(resource);
		LOG.info("Stored resource " + resource.getId() + " to collection " + collection.getName());
	}

	private Collection getRootCollection() throws XMLDBException {
		return getCollection(rootCollectionPath);
	}
	
	/**
	 * Create and initialise an instance of the database.
	 * 
	 * @throws XMLDBException
	 * 
	 * @throws Exception
	 *             if there is an database initialisation error.
	 */
	private static void initialiseDBase() throws XMLDBException {
		/*-- 
		 * Temporarily disable to avoid concurrent conflicts
		 * In future each transformation should access the default 
		 * collections and create/delete the custom ones! 
		 * --*/
		/*
		if(!isInitialized()) {
			throw new XMLDBException();
		} 

		Collection root = DatabaseManager.getCollection(rootCollectionPath, DBManager.user, DBManager.psw);

		CollectionManagementService mgtService = (CollectionManagementService) root.getService(
				COLLECTION_MGMT_SERVICE_NAME, COLLECTION_MGMT_SERVICE_VERSION);

	for (NewmoonManager.MODULE_NAME def_coll : NewmoonManager.MODULE_NAME.values()) {
		try{
			mgtService.removeCollection(def_coll.getCollectionName());
		} catch (Exception e) {
			
		} 	
	}

	for (NewmoonManager.MODULE_NAME def_coll : NewmoonManager.MODULE_NAME.values()) {
		try{
			mgtService.createCollection(def_coll.getCollectionName());
		} catch (Exception e) {
			
		} 
			
	}	 
		LOG.info("Successfully created default collections.");
				 */
	}

	/*
	 * public void startExist() throws XMLDBException, NewmoonException {
	 * //initEnv(); Database database; try { Class<?> cl =
	 * Class.forName("org.exist.xmldb.DatabaseImpl"); database = (Database)
	 * cl.newInstance(); //database.setProperty("create-database", "true");
	 * DatabaseManager.registerDatabase(database); running = true; // initialize
	 * the deafult DB initialiseDBase(); } catch (ClassNotFoundException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); } catch
	 * (InstantiationException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 */

	public void stopExist() throws XMLDBException {
		// shut down the database
		/*
		 * DatabaseInstanceManager manager = (DatabaseInstanceManager)
		 * rootCollection.getService( "DatabaseInstanceManager", "1.0");
		 * manager.shutdown();
		 */
	}

	public XQueryService getXQueryService() throws XMLDBException {
		XQueryService service = (XQueryService) getRootCollection().getService(XQUERY_SERVICE_SERVICE_NAME,
				XQUERY_SERVICE_SERVICE_VERSION);
		service.setProperty(INDENT_PROPERTY_NAME, YES);
		return service;
	}

	public void mapDBCollection(final Collection collection) throws XMLDBException {
		final Collection intCollection = collection == null ? getRootCollection() : collection;

		for (String resource : intCollection.listResources()) {
			System.out.println("parentCollection:" + intCollection.getParentCollection().getName() + " - Collection:"
					+ intCollection.getName() + " - Resource:" + resource);
		}
		String[] childrenName = intCollection.listChildCollections();
		for (String childName : childrenName) {
			mapDBCollection(intCollection.getChildCollection(childName));
		}
	}

	/**
	 * Returns a <code>Collection</code>'s child given its relative path.
	 * 
	 * @param collection
	 *            the parent Collection. If <code>null</code> the root
	 *            collection, that is the collection having path
	 *            {@link NewmoonManager.NM_PARAM#ROOT_COLLECTION_PATH}, is used.
	 * @param childName
	 *            path relative to the parent's path
	 **/
	public Collection getCollectionChild(Collection collection, String childName) throws XMLDBException {
		collection = collection == null ? getRootCollection() : collection;
		return collection.getChildCollection(childName);
	}
}
