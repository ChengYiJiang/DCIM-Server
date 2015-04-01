/**
 * 
 */
package com.raritan.tdz.reports.generate;

/**
 * @author prasanna
 *
 */
public interface ReportTaskProgressHandlerFactory {
	/**
	 * Create the progress handler based on reportId. If there is an 
	 * instance already created, then give that instance.
	 * @param reportId
	 * @param additionalArgs TODO
	 * @return
	 */
	public ReportTaskProgressHandler createProgressHandler(long reportId, Object additionalArgs);
}
