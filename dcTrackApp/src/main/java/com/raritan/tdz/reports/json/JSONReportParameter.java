package com.raritan.tdz.reports.json;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class JSONReportParameter {

	@JsonProperty("parameterId")
	public Long getReportParameterId() {
		return reportParameterId;
	}
	public void setReportParameterId(Long reportParameterId) {
		this.reportParameterId = reportParameterId;
	}
	@JsonProperty("templateParameterName")
	public String getTemplateParamName() {
		return templateParamName;
	}
	public void setTemplateParamName(String templateParamName) {
		this.templateParamName = templateParamName;
	}

	public String getUiPanelId() {
		return uiPanelId;
	}
	public void setUiPanelId(String uiPanelId) {
		this.uiPanelId = uiPanelId;
	}

	private Long reportParameterId;
	private String templateParamName;
	private String uiPanelId;
}
