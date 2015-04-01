/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * @author prasanna
 *
 */
public abstract class ReportTaskProgressManagerBase implements ReportTaskProgressManager {
	
	protected Map<Long, ReportTaskProgressHandler> progressHandlerMap = new HashMap<Long, ReportTaskProgressHandler>();
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportTaskProgressManager#getPercentComplete(long)
	 */
	@Override
	public int getPercentComplete(long reportId) {
		ReportTaskProgressHandler progressHandler = progressHandlerMap.get(reportId);
		return progressHandler != null ? progressHandler.getPercentComplete() : 0;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportTaskProgressManager#getURL(long)
	 */
	@Override
	public String getURL(long reportId) {
		ReportTaskProgressHandler progressHandler = progressHandlerMap.get(reportId);
		return progressHandler != null ? progressHandler.getURL() : "";
	}
	
	@Override
	public void cancel(long reportId) {
		ReportTaskProgressHandler progressHandler = progressHandlerMap.get(reportId);
		if (progressHandler != null) 
			progressHandler.cancel();
		//TODO: We have to throw an exception if the progressHandler is not found???
	}
	
	@Override
	public Errors getErrors(long reportId) {
		ReportTaskProgressHandler progressHandler = progressHandlerMap.get(reportId);
		return progressHandler != null ? progressHandler.getErrors() :  getErrorsObject();
	}
	
	
	@Override
	public Boolean isTaskRunning(long reportId) {
		ReportTaskProgressHandler progressHandler = progressHandlerMap.get(reportId);
		return progressHandler != null ? progressHandler.isRunning() : false;
	}

	private Errors getErrorsObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, BIRTReportTaskSetup.class.getName() );
		return errors;
	}
}
