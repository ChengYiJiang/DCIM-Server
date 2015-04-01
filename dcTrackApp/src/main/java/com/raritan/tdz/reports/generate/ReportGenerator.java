/**
 * 
 */
package com.raritan.tdz.reports.generate;

import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;

/**
 * This interface can be used to generate a report.
 * @author prasanna
 *
 */
public interface ReportGenerator {
	/**
	 * Given the criteria, generate a report
	 * @param sessionId
	 * @param criteriaDTO
	 * @return
	 * @throws Exception
	 */
	public ReportStatusDTO generateReport(String sessionId, ReportCriteriaDTO criteriaDTO) throws Exception;
	
	/**
	 * Given the reportId, get the status of the report.
	 * @param sessionId
	 * @param reportId
	 * @return
	 */
	public ReportStatusDTO getReportStatus(String sessionId, long reportId);
	
	/**
	 * Cancel the report for a specific reportId
	 * @param sessionId
	 * @param reportId
	 */
	public void cancelReportGeneration(String sessionId, long reportId);
}
