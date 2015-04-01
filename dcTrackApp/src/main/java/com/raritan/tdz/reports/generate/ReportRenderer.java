/**
 * 
 */
package com.raritan.tdz.reports.generate;

/**
 * This interface will run the report task.
 * @author prasanna
 *
 */
public interface ReportRenderer {
	/**
	 * Start the report task.
	 * @param task
	 * @throws Exception 
	 */
	public void run(Object task) throws Exception;
}
