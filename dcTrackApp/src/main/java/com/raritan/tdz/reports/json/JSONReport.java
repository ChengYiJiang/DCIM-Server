package com.raritan.tdz.reports.json;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class JSONReport{
	
	private long reportId;
	private String reportName;
	private String templateName;
	private Timestamp creationDate;
	private String description;
	private Set<JSONReportParameter> reportParameters = new HashSet<JSONReportParameter>(0);
	private Set<JSONReportConfig> reportConfig = new HashSet<JSONReportConfig>(0);

	@JsonProperty("reportName")
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	@JsonProperty("reportParameters")
	public Set<JSONReportParameter> getReportParameters() {
		return reportParameters;
	}

	public void setReportParameters(Set<JSONReportParameter> reportParameters) {
		this.reportParameters = reportParameters;
	}

	@JsonProperty("reportConfig")
	public Set<JSONReportConfig> getReportConfig() {
		return reportConfig;
	}

	public void setReportConfig(Set<JSONReportConfig> reportConfig) {
		this.reportConfig = reportConfig;
	}



	@JsonProperty("reportId")
	public long getReportId() {
		return reportId;
	}
	

	public void setReportId(long reportId) {
		this.reportId = reportId;
	}

	@JsonProperty("templateName")
	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	@JsonProperty("creationDate")
	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
