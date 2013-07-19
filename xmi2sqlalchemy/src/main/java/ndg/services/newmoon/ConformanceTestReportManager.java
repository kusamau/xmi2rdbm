package ndg.services.newmoon;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import ndg.common.exception.ResourceNotAvailable;
import ndg.common.jaxb.JAXBManager;

import org.apache.commons.lang.text.StrBuilder;

import conftest_result.newmoon.service.ndg._1.Result;
import conftest_result.newmoon.service.ndg._1.Results;

public class ConformanceTestReportManager {

	public List<ReportError> scanForErrors(InputStream conformanceReport) throws JAXBException, ResourceNotAvailable {
		List<ReportError> ret = new ArrayList<ReportError>();
		JAXBManager manager = JAXBManager.getInstance();
		Results report = manager.unmarshall(conformanceReport, Results.class);
		if (report.getResult() == null || report.getResult().size() == 0) {
			return ret;
		}
		StrBuilder appender = new StrBuilder();
		for (Result res : report.getResult()) {
			if (res.getFailed() != null) {
				String msgs = (res.getMessages() != null && res.getMessages().getMessage() != null && res.getMessages()
						.getMessage().size() > 0) ? appender
						.appendWithSeparators(res.getMessages().getMessage(), " - ").toString() : "";
				ret.add(new ReportError("Test num. " + res.getTest().getNumber(), res.getFailed(), msgs));
			}
		}
		return ret;
	}
}
