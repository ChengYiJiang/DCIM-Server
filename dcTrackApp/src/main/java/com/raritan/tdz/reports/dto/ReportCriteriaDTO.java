/**
 * 
 */
package com.raritan.tdz.reports.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

/**
 * This DTO will be used by the client when user wants to generate
 * a report.
 * @author prasanna
 *
 */
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class ReportCriteriaDTO {
	private long reportId;
	private Map<String, Object> reportParams;
	
	@JsonProperty("reportId")
	public long getReportId() {
		return reportId;
	}
	public void setReportId(long reportId) {
		this.reportId = reportId;
	}
	
	@JsonProperty("reportParams")
	public Map<String, Object> getReportParams() {
		return reportParams;
	}
	public void setReportParams(Map<String, Object> reportParams) {
		this.reportParams = reportParams;
	}
	
	@Override
	public String toString() {
		return "ReportCriteriaDTO [reportId=" + reportId + ", reportParams="
				+ reportParams + "]";
	}
	
	public ReportCriteriaDTO clone(){
		ReportCriteriaDTO clonnedCritiera = new ReportCriteriaDTO();
		clonnedCritiera.setReportParams(new HashMap<String,Object>());
		clonnedCritiera.setReportId(getReportId());
		
		return clonnedCritiera;
	}
}
