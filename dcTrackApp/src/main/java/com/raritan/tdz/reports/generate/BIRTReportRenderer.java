/**
 * 
 */
package com.raritan.tdz.reports.generate;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.reports.dao.ReportsDAO;
import com.raritan.tdz.reports.eventhandler.ReportConstants;

/**
 * @author prasanna
 *
 */
public class BIRTReportRenderer implements ReportRenderer {
	
	@Autowired
	ReportsDAO reportsDAO;
	
	Logger log = Logger.getLogger(getClass());

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportTaskHandler#run(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void run(Object task) throws Exception {
		IRunAndRenderTask birtTask = (IRunAndRenderTask) task;
		
		//Adding the session here because the renderer is a thread of its own. This will always 
		//create a new session since this is in a different thread
		birtTask.getAppContext().put(ReportConstants.SESSION_ID, reportsDAO.getSession());
		
		try {
			birtTask.run();
		} catch (Exception e){
			log.error("Report Task Failed. Parameters sent are: " + birtTask.getParameterValues());
			log.error("Report Task Failed. Errors are: " + birtTask.getErrors());
			if (log.isDebugEnabled())
				e.printStackTrace();
			Errors errors = (Errors) birtTask.getAppContext().get(BIRTReportTaskSetup.ERRORS_OBJECT);
			Object[] errorArgs = { birtTask.getAppContext().get(BIRTReportTaskSetup.REPORT_NAME) };
			errors.reject("Reports.generateReportFailed", errorArgs, "Report Generation failed");
			throw e;
		}
	}

}
