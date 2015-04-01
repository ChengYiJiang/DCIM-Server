package com.raritan.tdz.reports.generator;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.raritan.tdz.reports.dao.ReportsDAO;
import com.raritan.tdz.reports.eventhandler.ReportConstants;

public class BirtGenerateReport implements GenerateReport {

	@Autowired
	private ReportsDAO reportsDAO;
	
	@Autowired
	private IReportEngine birtReportEngine;
	
	@SuppressWarnings("unchecked")
	public void generateReport(String reportDesignFile,
			String reportOutputFile, String reportFormat, Map<String, Object> reportParameters) throws Throwable {

		ApplicationContext sprCtx = ContextAccess.getApplicationContext();
		
		EngineConfig birtConfig = birtReportEngine.getConfig();
		// birtConfig.setLogConfig(null, Level.OFF);
		Logger birtLogger = birtConfig.getLogger();
		birtLogger.setLevel(Level.OFF);
		
		birtConfig.getAppContext().put("reportContextAwareKey", sprCtx);
		Session session = reportsDAO.getSession();
		birtConfig.getAppContext().put(ReportConstants.SESSION_ID, session);
		
		ReportGeneratorSetup setup = new ReportGeneratorSetup((ReportEngine) birtReportEngine, reportFormat);
		setup.setReportGeneratorProgressMonitor(new ReportGeneratorProgressMonitor());
		IRunAndRenderTask task = setup.setup(reportDesignFile, reportOutputFile, reportParameters);
   
		ReportGeneratorExecutor birtExec = new ReportGeneratorExecutor(task);
   
		ExecutorService service = Executors.newSingleThreadExecutor();
		@SuppressWarnings("rawtypes")
		Future future = service.submit(birtExec);
   
		@SuppressWarnings("unused")
		Object success = future.get();
   
		service.shutdown();
   
		// birtReportEngine.destroy();
   
		System.out.println("All Went well");

	}
	
	public void cleanup() {
		
		birtReportEngine.destroy();
		
	}

}
