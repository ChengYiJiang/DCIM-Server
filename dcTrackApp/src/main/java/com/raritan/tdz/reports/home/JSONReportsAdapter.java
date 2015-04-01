package com.raritan.tdz.reports.home;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportConfig;
import com.raritan.tdz.reports.json.JSONReport;
import com.raritan.tdz.reports.json.JSONReportConfig;

public interface JSONReportsAdapter {

	Map<String, Object> adaptReportToJSONArray(Report report);

	Map<String, Object> adaptReportsListToJSONArray(
			List<Report> allReports);
	
	Set<ReportConfig> adaptJSONReportConfigToDomain( Set<JSONReportConfig> jsonReportConfig ) throws BusinessValidationException;

	Map<String, Object> adaptReportConfigSetToJSONArray(Set<ReportConfig> reportConfig);
}
