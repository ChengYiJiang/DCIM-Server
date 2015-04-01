/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.eclipse.birt.report.engine.api.IProgressMonitor;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.reports.dao.ReportsDAO;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.dto.ReportCriteriaDTO;


/**
 * @author prasanna
 *
 */
public class BIRTReportTaskSetup implements ReportTask {
	
	public static String ERRORS_OBJECT = "errors"; 
	public static String REPORT_NAME = "reportName";
	
	@Autowired(required=false)
	private ServletContext servletContext;
	
	@Autowired
	private IReportEngine reportEngine;
	
	@Autowired
	private ReportTaskProgressHandlerFactory reportEngineFactory;

	@Autowired
	private ReportsDAO reportDao;
	
	private static String designPath = "/WEB-INF/reports/designs/";
	
	private String designPathPrefix = "";
	
	private String reportContextAwareKey = "";
	
	private String userSessionId;
	
	IRenderOption renderOption;
	
	public BIRTReportTaskSetup(IRenderOption renderOption, String userSessionId) {
		this.renderOption = renderOption;
		this.userSessionId = userSessionId;
	}
	
	public String getDesignPathPrefix() {
		return designPathPrefix;
	}

	public void setDesignPathPrefix(String designPathPrefix) {
		this.designPathPrefix = designPathPrefix;
	}



	public String getReportContextAwareKey() {
		return reportContextAwareKey;
	}

	public void setReportContextAwareKey(String reportContextAwareKey) {
		this.reportContextAwareKey = reportContextAwareKey;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportTaskSetup#setupTask(com.raritan.tdz.reports.dto.ReportCriteriaDTO)
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Object createAndSetup(ReportCriteriaDTO criteriaDTO) throws Exception {
		// Assuming the criteriaDTO is in the format that BIRT expects
		
		Report report = reportDao.getReport(criteriaDTO.getReportId());
		
		
		StringBuilder reportDesignFile = new StringBuilder();
		reportDesignFile
			.append(designPathPrefix)
			.append(designPath)
			.append(report.getTemplateName());
		
		String reportDesignFileStr = reportDesignFile.toString();
		if (servletContext != null) reportDesignFileStr = servletContext.getRealPath(reportDesignFile.toString());
		
		// Create the Design task
		IReportRunnable design = reportEngine.openReportDesign(reportDesignFileStr);
		
		// Create the BIRT task
		IRunAndRenderTask task =  reportEngine.createRunAndRenderTask(design);
		
		String outputPath = generateOutputPath(criteriaDTO.getReportId());
		renderOption.setOutputFileName(outputPath);
		task.setRenderOption(renderOption);
		
		List progressHandlerArgs = new ArrayList();
		progressHandlerArgs.add(task);
		progressHandlerArgs.add(outputPath);
		task.setProgressMonitor((IProgressMonitor)reportEngineFactory.createProgressHandler(criteriaDTO.getReportId(), progressHandlerArgs));
		
		task.getAppContext().put(reportContextAwareKey, ReportSpringContextAwareMgr.get(reportContextAwareKey));
		task.getAppContext().put(ERRORS_OBJECT, getErrorsObject());
		task.getAppContext().put(REPORT_NAME, report.getReportName());
		
		//Setup the parameters. Please note that the parameters are in the format
		//that report render expects. This is done in the translator.
		task.setParameterValues(criteriaDTO.getReportParams() != null ? criteriaDTO.getReportParams() : new HashMap<Long, Object>() );
		
		return task;
	}
	
	private Object getErrorsObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, this.getClass().getName() );
		return errors;
	}

	public String generateOutputPath(long reportId) {
		
		Report report = reportId > 0 ? reportDao.getReport(reportId): null;
		
		String prefix = "";
		if (servletContext != null) prefix = servletContext.getRealPath("/") + "../dcTrackReports/";
		
		StringBuilder outputPathBuilder = new StringBuilder();
		outputPathBuilder.append(prefix).append(report != null ? report.getReportName() : "*")
		.append(" ")
		.append(new Date().getTime());
		
		
		
		return (outputPathBuilder.toString()).replace(" ", "_") + ".pdf";
	}

}
