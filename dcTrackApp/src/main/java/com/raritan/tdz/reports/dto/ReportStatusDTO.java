/**
 * 
 */
package com.raritan.tdz.reports.dto;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.springframework.validation.Errors;

/**
 * This provides the current status of the report generation
 * The URL for the report will be filled up after the report generation
 * is complete.
 * @author prasanna
 *
 */
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class ReportStatusDTO {

	private long reportId;
	private int progress;
	private String url;
	
	//This will not be provided via JSON
	//This is only used internally.
	private Errors errors;
	
	@JsonProperty("reportId")
	public long getReportId() {
		return reportId;
	}
	public void setReportId(long reportId) {
		this.reportId = reportId;
	}
	
	@JsonProperty("progress")
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	@JsonProperty("url")
	public String getUrl() {
		return url != null && !url.isEmpty() ? url : null;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public Errors getErrors() {
		return errors;
	}
	public void setErrors(Errors errors) {
		this.errors = errors;
	}
	
	@Override
	public String toString() {
		return "ReportStatusDTO [reportId=" + reportId + ", progress="
				+ progress + ", url=" + url + "]";
	}
	
	
}
