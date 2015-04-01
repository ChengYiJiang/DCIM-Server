package com.raritan.tdz.reports.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportConfig;
import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;
import com.raritan.tdz.reports.home.JSONReportsAdapter;
import com.raritan.tdz.reports.home.ReportsHome;
import com.raritan.tdz.reports.json.JSONReportConfig;
import com.raritan.tdz.reports.validators.ReportsSettingsCommonValidator;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

public class ReportsServiceImpl implements ReportsServiceRESTAPI {

	@Autowired(required=true)
	ReportsHome reportsHome;
	
    @Autowired(required=true)
    private JSONReportsAdapter jsonReportsAdapter;
	
	@Autowired( required=true)
	ReportsSettingsCommonValidator reportsValidator;

	
	/*** REST APIs IMPLEMENTATION ***/
	@Override
	@Transactional(readOnly=true)
	public Map<String, Object> getReportDetailsExt(Long reportId, UserInfo userInfo)
			throws BusinessValidationException {
	
		Report report = reportsHome.getReportForUser(reportId, userInfo);
		return jsonReportsAdapter.adaptReportToJSONArray( report );
	}

	@Override
	@Transactional(readOnly=true)
	public Map<String, Object> getAllReports(UserInfo userInfo) 
			throws BusinessValidationException{
		
		List<Report> allReports = reportsHome.getAllReportsForUser(userInfo);
		
		return jsonReportsAdapter.adaptReportsListToJSONArray( allReports );
	}

	
	@Transactional
	@Override
	public Map<String, Object> updateReportConfiguration(Long reportId, 
			Set<JSONReportConfig> jsonReportConfig, UserInfo userInfo) throws BusinessValidationException {
		
		reportsValidator.validateRequiredFields(jsonReportConfig);
		Set<ReportConfig> reportConfig = jsonReportsAdapter.adaptJSONReportConfigToDomain(jsonReportConfig);
		reportConfig = reportsHome.updateReportConfiguration(reportId, reportConfig, userInfo);
		
		return jsonReportsAdapter.adaptReportConfigSetToJSONArray(reportConfig);
	}


	@Transactional(readOnly=true)
	@Override
	public ReportStatusDTO generateReport(ReportCriteriaDTO criteriaDTO)
			throws BusinessValidationException {
		return reportsHome.generateReport(criteriaDTO, RESTAPIUserSessionContext.getUser());
	}

	@Override
	public ReportStatusDTO getReportStatus(Long reportId)
			throws BusinessValidationException {
		return reportsHome.getReportStatus(reportId, RESTAPIUserSessionContext.getUser());
	}

	@Override
	public void cancelReportGeneration(Long reportId)
			throws BusinessValidationException {
		reportsHome.cancelReportGeneration(reportId, RESTAPIUserSessionContext.getUser());
	}

}
