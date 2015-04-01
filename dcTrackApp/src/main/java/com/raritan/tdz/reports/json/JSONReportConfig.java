package com.raritan.tdz.reports.json;

import java.sql.Timestamp;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class JSONReportConfig {

	@JsonProperty("reportConfigId")
	public Long getReportConfigId() {
		return reportConfigId;
	}
	public void setReportConfigId(Long reportConfigId) {
		this.reportConfigId = reportConfigId;
	}
	
	@JsonProperty("configName")	
	public String getConfigName() {
		return configName;
	}
	public void setConfigName(String configName) {
		this.configName = configName;
	}
	
	@JsonProperty("creationDate")
	public Timestamp getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	
	@JsonProperty("reportConfigDetails")
	public Set<JSONReportConfigDetails> getReportConfigDetails() {
		return reportConfigDetails;
	}
	public void setReportConfigDetails(Set<JSONReportConfigDetails> reportConfigDetails) {
		this.reportConfigDetails = reportConfigDetails;
	}
	
	private Long reportConfigId;
	private String configName;
	private Timestamp creationDate;
	private Set<JSONReportConfigDetails> reportConfigDetails;
	
	
}
