/**
 * 
 */
package com.raritan.tdz.reports.generate;

import com.raritan.tdz.reports.dto.ReportCriteriaDTO;

/**
 * This interface will setup a report task.
 * @author prasanna
 *
 */
public interface ReportTask {
	/**
	 * Sets up a report task and returns the task object
	 * @param criteriaDTO
	 * @return
	 * @throws Exception TODO
	 */
	public Object createAndSetup(ReportCriteriaDTO criteriaDTO) throws Exception;
}
