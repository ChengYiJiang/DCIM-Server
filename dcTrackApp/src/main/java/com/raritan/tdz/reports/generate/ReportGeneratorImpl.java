/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.util.Map;

import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.springframework.integration.annotation.Payload;

import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;
import com.raritan.tdz.reports.generate.exceptions.ReportAlreadyInProgressException;

/**
 * Report Generator using BIRT
 * @author prasanna
 *
 */
public class ReportGeneratorImpl implements ReportGenerator {
	
	private ReportTask reportTaskSetup;
	private ReportRenderer reportRenderer;
	private ReportTaskProgressManager reportProgressManager;
	

	public ReportGeneratorImpl(
			ReportTask reportTaskSetup, 
			ReportRenderer reportRenderer,
			ReportTaskProgressManager reportProgressManager){
		this.reportTaskSetup = reportTaskSetup;
		this.reportRenderer = reportRenderer;
		this.reportProgressManager = reportProgressManager;
	}
	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportGenerator#generateReport(com.raritan.tdz.reports.dto.ReportCriteriaDTO)
	 */
	@Override
	public ReportStatusDTO generateReport(@Payload("#this[0]") String sessionId, @Payload("#this[1]") ReportCriteriaDTO criteriaDTO) throws Exception {
		
		//Check to see if report task is already running
		if (reportProgressManager.isTaskRunning(criteriaDTO.getReportId()))
			throw new ReportAlreadyInProgressException();
		
		// setup the report task (we may translate the report critiera)
		Object reportTask = reportTaskSetup.createAndSetup(criteriaDTO);
		
		// render
		reportRenderer.run(reportTask);
		
		// get the progress
		return getReportStatus(null, criteriaDTO.getReportId());
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportGenerator#getReportStatus(long)
	 */
	@Override
	public ReportStatusDTO getReportStatus(@Payload("#this[0]") String sessionId, @Payload("#this[1]") long reportId) {
		ReportStatusDTO statusDTO = new ReportStatusDTO();
		statusDTO.setReportId(reportId);
		statusDTO.setErrors(reportProgressManager.getErrors(reportId));
		statusDTO.setProgress(reportProgressManager.getPercentComplete(reportId));
		statusDTO.setUrl(reportProgressManager.getURL(reportId));
		
		return statusDTO;
	}


	@Override
	public void cancelReportGeneration(@Payload("#this[0]") String sessionId,  @Payload("#this[1]") long reportId) {
		reportProgressManager.cancel(reportId);
	}

}
