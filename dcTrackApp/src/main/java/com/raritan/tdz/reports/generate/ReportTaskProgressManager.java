/**
 * 
 */
package com.raritan.tdz.reports.generate;

import org.springframework.validation.Errors;

/**
 * This interface will provide a client with the progress 
 * of the report task.
 * @author prasanna
 *
 */
public interface ReportTaskProgressManager {
	/**
	 * Gets the progress on the report task.
	 * @return
	 */
	public int getPercentComplete(long reportId);
	
	/**
	 * Get the output URL to be displayed on the client.
	 * @return
	 */
	public String getURL(long reportId);
	
	/**
	 * Cancel the report task for a given reportId
	 * @param reportId
	 */
	public void cancel(long reportId);
	
	/**
	 * Get the errors object
	 * @param reportId
	 * @return
	 */
	public Errors getErrors(long reportId);
	
	/**
	 * Checks if the task is running or not. Returns true if it is
	 * @param reportId
	 * @return
	 */
	public Boolean isTaskRunning(long reportId);
}
