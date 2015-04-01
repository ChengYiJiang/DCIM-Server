package com.raritan.tdz.reports.json;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class JSONReportConfigDetails {
	@JsonProperty("fieldName")
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	@JsonProperty("fieldValue")
	public String getFieldValue() {
		return fieldValue;
	}
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
	@JsonProperty("isParameter")
	public Boolean getIsParameter() {
		return isParameter;
	}
	public void setIsParameter(Boolean isParameter) {
		this.isParameter = isParameter;
	}
	
	@JsonProperty("conditionLookup")
	public String getConditionLookupValue() {
		return conditionLookupValue;
	}
	public void setConditionLookupValue(String conditionLookupValue) {
		this.conditionLookupValue = conditionLookupValue;
	}
	
	@JsonProperty("uiPanelId")
	public String getUiPanelId() {
		return uiPanelId;
	}
	public void setUiPanelId(String uiPanelId) {
		this.uiPanelId = uiPanelId;
	}
	
	@JsonProperty("reportConfigDetailId")
	public Long getReportConfigDetailId() {
		return reportConfigDetailId;
	}
	public void setReportConfigDetailId(Long reportConfigDetailId) {
		this.reportConfigDetailId = reportConfigDetailId;
	}

	private Long reportConfigDetailId;
	private String fieldName;
	private String fieldValue;
	private Boolean isParameter;
	private String conditionLookupValue;
	private String uiPanelId;
	
}
