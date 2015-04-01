package com.raritan.tdz.reports.home;

import java.util.List;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportConfig;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;


public interface ReportsHome {
	/**
	 * Get one report
	 * @param reportId the report Id
	 * @return Report object
	 */	
	public Report getReportForUser(Long reportId, UserInfo userInfo);

	/**
	 * Get all the reports
	 * @param None
	 * @return List of Report objects
	 */	
	public List<Report> getAllReportsForUser(UserInfo userInfo);
	
	/**
	 * Save the filter associated with the Report
	 * @param report Report object
	 * @return none
	 */	
	public Set<ReportConfig> updateReportConfiguration(Long reportId, 
			Set<ReportConfig> reportconfig, UserInfo userInfo) throws BusinessValidationException ;
	

	/**
	 * 
	 * @param reportCriteria
	 * @param userInfo
	 * @return
	 * @throws BusinessValidationException TODO
	 */
	ReportStatusDTO generateReport(ReportCriteriaDTO reportCriteria, UserInfo userInfo) throws BusinessValidationException;
	
	/**
	 * 
	 * @param reportId
	 * @param userInfo
	 * @return
	 * @throws BusinessValidationException TODO
	 */
	ReportStatusDTO getReportStatus(long reportId, UserInfo userInfo) throws BusinessValidationException;
	
	/**
	 * 
	 * @param reportId
	 * @param userInfo
	 * @throws BusinessValidationException TODO
	 */
	void cancelReportGeneration(long reportId, UserInfo userInfo) throws BusinessValidationException;

}
