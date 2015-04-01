/**
 * 
 */
package com.raritan.tdz.reports.generate;

import org.springframework.validation.Errors;

/**
 * @author prasanna
 *
 */
public interface ReportTaskProgressHandler {
	/**
	 * Gets the progress on the report task.
	 * @return
	 */
	public int getPercentComplete();
	
	/**
	 * Get the output URL to be displayed on the client.
	 * @return
	 */
	public String getURL();
	
	/**
	 * Cancel the current report task
	 */
	public void cancel();
	
	
	/**
	 * Get any errors during the report generation so that we can show that 
	 * to user.
	 * @return
	 */
	public Errors getErrors();

	/**
	 * Checks if the report generation is running. Returns true if it is.
	 * @return
	 */
	public Boolean isRunning();
	
	/**
	 * Performs any cleanup before releasing this object to memory pool
	 */
	public void cleanup();
}
