package com.raritan.tdz.reports.service;

import java.util.Map;
import java.util.Set;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;
import com.raritan.tdz.reports.json.JSONReport;
import com.raritan.tdz.reports.json.JSONReportConfig;

public interface ReportsServiceRESTAPI {
	
	Map<String, Object> getReportDetailsExt(Long reportId, UserInfo userInfo) throws BusinessValidationException;
	Map<String, Object> getAllReports(UserInfo userInfo) throws BusinessValidationException;
	Map<String, Object> updateReportConfiguration( Long reportId, 
			Set<JSONReportConfig> reportConfig, UserInfo userInfo) throws BusinessValidationException;	
	ReportStatusDTO generateReport(ReportCriteriaDTO criteriaDTO) throws BusinessValidationException;
	
	ReportStatusDTO getReportStatus(Long reportId) throws BusinessValidationException;
	
	void cancelReportGeneration(Long reportId) throws BusinessValidationException;
}
