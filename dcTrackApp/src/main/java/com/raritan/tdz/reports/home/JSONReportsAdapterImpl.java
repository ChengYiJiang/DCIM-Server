package com.raritan.tdz.reports.home;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportConfig;
import com.raritan.tdz.reports.domain.ReportConfigDetails;
import com.raritan.tdz.reports.domain.ReportParameter;
import com.raritan.tdz.reports.json.JSONReport;
import com.raritan.tdz.reports.json.JSONReportConfig;
import com.raritan.tdz.reports.json.JSONReportConfigDetails;
import com.raritan.tdz.reports.json.JSONReportParameter;
import com.raritan.tdz.reports.validators.ReportsSettingsCommonValidator;



public class JSONReportsAdapterImpl implements JSONReportsAdapter {

	@Autowired( required=true)
	ReportsSettingsCommonValidator reportsValidator;

	@Override
	public Map<String, Object> adaptReportToJSONArray(Report r) {
		List<JSONReport> jsonReportList = new ArrayList<JSONReport>();
		if( r != null ){
			JSONReport jsonR = convertDomainToJSONReport(r);
			jsonReportList.add(jsonR);
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("reportSettings", jsonReportList );
		return ret;
	}

	private JSONReport convertDomainToJSONReport(Report r) {
		if( r == null ) return null;
		JSONReport jsonR = new JSONReport();
		jsonR.setReportName(r.getReportName());
		jsonR.setReportId(r.getReportId());
		jsonR.setCreationDate(r.getCreationDate());
		jsonR.setDescription(r.getDescription());
		jsonR.setTemplateName(r.getTemplateName());
		Set<JSONReportParameter> jsonRPSet = 
				convertDomainToJSONReportParams( r.getReportParameters());
		jsonR.setReportParameters(jsonRPSet);

		Set<JSONReportConfig> jsonRCSet = 
				convertDomainToJsonReportConfigSet( r.getReportConfig());
		jsonR.setReportConfig(jsonRCSet);

		return jsonR;
	}

	private Set<JSONReportConfig> convertDomainToJsonReportConfigSet(
			Set<ReportConfig> rCSet) {
		Set<JSONReportConfig> jRCSet = new HashSet<JSONReportConfig>(0);
		if( rCSet != null && rCSet.size() > 0 ){
			for( ReportConfig rC : rCSet ){
				JSONReportConfig jRC = new JSONReportConfig();
				jRC.setReportConfigId(rC.getReportConfigId());
				jRC.setCreationDate(rC.getCreationDate());
				jRC.setConfigName(rC.getConfigName());
				Set<JSONReportConfigDetails> configDetails = 
						convertDomainToJsonReportConfigDetailsSet(rC.getReportConfigDetails());
				jRC.setReportConfigDetails(configDetails);
				jRCSet.add(jRC);
			}
		}
		return jRCSet;
	}

	private Set<JSONReportConfigDetails> convertDomainToJsonReportConfigDetailsSet(
			Set<ReportConfigDetails> rCDSet) {
		Set<JSONReportConfigDetails> jRCDSet = new HashSet<JSONReportConfigDetails>(0);
		if( rCDSet != null && rCDSet.size() > 0 ){
			for( ReportConfigDetails rCD : rCDSet) {
				JSONReportConfigDetails oneJsonReportConfigDetail = 
						convertDomainToJsonReportConfigDetails(rCD);
				jRCDSet.add(oneJsonReportConfigDetail);
			}
		}
		return jRCDSet;
	}

	private JSONReportConfigDetails convertDomainToJsonReportConfigDetails(
			ReportConfigDetails rCD) {
		if( rCD == null ) return null;
		JSONReportConfigDetails jRCD = new JSONReportConfigDetails();
		LksData conditionLookup = rCD.getConditionLookup();
		if( conditionLookup != null ){
			jRCD.setConditionLookupValue(conditionLookup.getLkpValue());
		}
		jRCD.setUiPanelId(rCD.getUiPanelId());
		jRCD.setFieldName(rCD.getFieldName());
		jRCD.setFieldValue(rCD.getFieldValue());
		jRCD.setReportConfigDetailId(rCD.getReportConfigDetailId());
		jRCD.setIsParameter(rCD.getIsParameter());
		return jRCD;
	}

	private Set<JSONReportParameter> convertDomainToJSONReportParams(
			Set<ReportParameter> rPSet) {
		Set<JSONReportParameter> jRPSet = new HashSet<JSONReportParameter>(0);
		if( rPSet != null && rPSet.size() > 0 ){
			for( ReportParameter rP : rPSet ){
				JSONReportParameter jRP = new JSONReportParameter();
				jRP.setReportParameterId(rP.getReportParameterId());
				jRP.setTemplateParamName(rP.getTemplateParamName());
				jRP.setUiPanelId(rP.getUiPanelId());
				jRPSet.add( jRP);
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> adaptReportsListToJSONArray(
			List<Report> allReports) {
		Map<String, Object> ret = new HashMap<String, Object>();
		List<JSONReport> retJson = new ArrayList<JSONReport>();

		if( allReports != null && allReports.size() > 0 ){
			for( Report report : allReports ){
				JSONReport jsonReport = convertDomainToJSONReport(report);
				retJson.add( jsonReport);
			}
		}

		ret.put("reportSettings", retJson);
		return ret;
	}

	@Override
	public Map<String, Object> adaptReportConfigSetToJSONArray(
			Set<ReportConfig> allReportConfigs) {
		Map<String, Object> ret = new HashMap<String, Object>();
		Set<JSONReportConfig> retJson = new HashSet<JSONReportConfig>();

		if( allReportConfigs != null && allReportConfigs.size() > 0 ){
			retJson = convertDomainToJsonReportConfigSet(allReportConfigs);
		}

		ret.put("reportConfig", retJson);
		return ret;
	}


	public Set<ReportConfig> adaptJSONReportConfigToDomain(
			Set<JSONReportConfig> jsonReportConfigs) throws BusinessValidationException {
		Set<ReportConfig> reportConfigs = new HashSet<ReportConfig>(0);
		if( jsonReportConfigs != null && jsonReportConfigs.size() > 0 ){
			for( JSONReportConfig oneJsonReportConfig : jsonReportConfigs){
				ReportConfig oneReportConfig = new ReportConfig();
				oneReportConfig.setConfigName(oneJsonReportConfig.getConfigName());

				Date date = new Date();
				oneReportConfig.setCreationDate(new Timestamp(date.getTime()));

				Set<ReportConfigDetails> reportConfigDetails = 
						adaptJSONReportConfigDetailsToDomain(oneJsonReportConfig.getReportConfigDetails(), 
						oneReportConfig);

				oneReportConfig.setReportConfigDetails(reportConfigDetails);

				reportConfigs.add(oneReportConfig);
			}
		}
		return reportConfigs;
	}

	private Set<ReportConfigDetails> adaptJSONReportConfigDetailsToDomain(
			Set<JSONReportConfigDetails> jRCDSet, ReportConfig rC) throws BusinessValidationException {
		Set<ReportConfigDetails> rCDSet = new HashSet<ReportConfigDetails>(0);
		if( jRCDSet != null && jRCDSet.size() > 0 ){
			for( JSONReportConfigDetails jRCD : jRCDSet ){
				ReportConfigDetails rCD = new ReportConfigDetails();

				rCD.setFieldName(jRCD.getFieldName());
				rCD.setFieldValue(jRCD.getFieldValue());
				rCD.setIsParameter( jRCD.getIsParameter() != null ? jRCD.getIsParameter() : false );

				String conditionLkp = jRCD.getConditionLookupValue();
				if( conditionLkp != null && conditionLkp.length() > 0 ){
					LksData conditionLookup = 
							reportsValidator.validateAndGetConditionLookup(conditionLkp);
					rCD.setConditionLookup(conditionLookup);
				}

				rCD.setUiPanelId(jRCD.getUiPanelId());

				rCD.setReportConfig(rC);
				rCDSet.add(rCD);
			}
		}

		return rCDSet;
	}
}
