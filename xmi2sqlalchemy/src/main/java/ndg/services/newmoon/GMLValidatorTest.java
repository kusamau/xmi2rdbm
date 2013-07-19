package ndg.services.newmoon;

import java.util.HashMap;
import java.util.Map;

import ndg.services.newmoon.gml.ValidateXMIModel;

public class GMLValidatorTest {
		
	
	//@Test
	public final void testParseXmlFile() {
		//InputStream inputXML = NmParser.class.getClassLoader().getResourceAsStream("CEDA_Model/CEDA_ModelXMI.xml");
		//InputStream inputAddClass = NmParser.class.getClassLoader().getResourceAsStream("CEDA_Model/AdditionalClasses.txt");		
		//File baseDir = new File(GenerateModuleHelper.class.getClassLoader().getResource(".").getPath());
		
		Map<NewmoonManager.NM_PARAM, String> params = new HashMap<NewmoonManager.NM_PARAM, String>();
		//params.put(NewmoonManager.NM_PARAM.EXECUTION_DIR, GenerateModuleHelper.class.getClassLoader().getResource(".").getPath());
		params.put(NewmoonManager.NM_PARAM.XMI_DOC, "CEDA_Model/CEDA_Model.xml");
		
		ValidateXMIModel validator = new ValidateXMIModel();
		try {
			validator.execute(params);
		} catch (NewmoonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
