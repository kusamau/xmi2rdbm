package ngn.services.newmoon;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Future;

import ndg.services.newmoon.NewmoonException;
import ndg.services.newmoon.NewmoonManager;
import ndg.services.newmoon.NmParser;
import ndg.services.newmoon.UMLElementsArchive;
import ndg.services.newmoon.XMIParser;
import ndg.services.newmoon.collect.CollectClasses;
import ndg.services.newmoon.velocity.python.GenerateClasses;
import ndg.services.newmoon.velocity.python.GenerateFromClassModel;
import ndg.services.newmoon.velocity.python.GenerateModuleHelper;
import ndg.services.newmoon.velocity.python.GenerateTables;
import ndg.services.newmoon.xmiModel.UMLClass;
import ndg.services.newmoon.xmiModel.UMLModel;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestsTester {

	//EXECUTION_DIR is where the incoming file/ZIP is copied
	//XMI_DOC is the name of the main XMI file
	public enum NM_PARAM {
		XMI_DOC("xmi-path"), REPORT_NAME("reportname"), WORKING_DIRECTORY("working-directory"), DEPENDENCY_REGISTER_PATH(
				"dependency-register-path"), GLM_VERSION("gml-version"), ROOT_COLLECTION_URI("root-collection-uri"), XMLDB_USER(
				"xmldb-user"), XMLDB_PASSWORD("xmldb-password"), ROOT_COLLECTION_PATH("root-collection-path"), DEFAULT_ENCODE_REPORT_NAME(
				"EncodeReport"), DEFAULT_CONFORMANCE_REPORT_NAME("ConformanceTestReport"), CONFORMANCE_TEST_DIR(
				"conformanceDir"), ENCODE_DIR("encodeDir"), RESOURCE_NAME("defaultResourceName"), OUTPUT_DIR(
				"exportDir"), EXECUTION_DIR("execution-directory");

		private final String name;

		private NM_PARAM(String name) {
			this.name = name;
		}

		public String getParamName() {
			return this.name;
		}
	}

	
	public final void testParseinputAddClassFile() {
		InputStream inputAddClass = NmParser.class.getClassLoader().getResourceAsStream("CEDA_Model/AdditionalClasses.txt");
		if (inputAddClass != null) {
			Scanner sc = new Scanner(inputAddClass);
			while (sc.hasNext()) { 					
				System.out.println(sc.next());
			}
		}
	}
		
	//@Test
	public final void parseTestFiles() {

//		for (int index = 1 ; index < 10 ; index++) {
//			testParseXmlFile("TestModel0" + index);
//		}
//		
//		//Pigrizia
//		testParseXmlFile("TestModel10");
//		testParseXmlFile("TestModel11");		
//		testParseXmlFile("TestModel12");
		testParseXmlFile("TestModel13");		
	}
	
	public final void testParseXmlFile(String name) {
		//InputStream inputXML = NmParser.class.getClassLoader().getResourceAsStream("CEDA_Model/CEDA_ModelXMI.xml");
		//InputStream inputAddClass = NmParser.class.getClassLoader().getResourceAsStream("CEDA_Model/AdditionalClasses.txt");		
		//File baseDir = new File(GenerateModuleHelper.class.getClassLoader().getResource(".").getPath());
		Map<NewmoonManager.NM_PARAM, String> params = new HashMap<NewmoonManager.NM_PARAM, String>();
		params.put(NewmoonManager.NM_PARAM.EXECUTION_DIR, GenerateModuleHelper.class.getClassLoader().getResource(".").getPath());
		params.put(NewmoonManager.NM_PARAM.XMI_DOC, "sTests/" + name + ".xml");
		params.put(NewmoonManager.NM_PARAM.OUTPUT_DIR, name);
		if (name.equalsIgnoreCase("TestModel13")) {
			params.put(NewmoonManager.NM_PARAM.XMI_ADD_CONSTRAINTS, "sTests/Test13Constraints.txt");	
		}
				
		
		XMIParser umlToDB = new XMIParser();
		try {
			umlToDB.execute(params);
		} catch (NewmoonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
