package com.raritan.tdz.reports.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="dct_report_parameters")
public class ReportParameter implements Serializable{

	private static final long serialVersionUID = -2493632131010246523L;

	public ReportParameter(){}

	public ReportParameter(long reportParameterId, Report report,
			String templateParamName) {
		super();
		this.reportParameterId = reportParameterId;
		this.report = report;
		this.templateParamName = templateParamName;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_report_parameters_seq")
	@SequenceGenerator(name="dct_report_parameters_seq", sequenceName="dct_report_parameters_report_parameter_id_seq", allocationSize=1)
	@Column(name="report_parameter_id")
	private Long reportParameterId;
	public Long getReportParameterId() {
		return reportParameterId;
	}

	public void setReportParameterId(Long reportParameterId) {
		this.reportParameterId = reportParameterId;
	}
	
	@ManyToOne(fetch=FetchType.LAZY,targetEntity=Report.class)
	@JoinColumn(name="report_id")
	private Report report;
	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	@Column(name="template_param_name")
	private String templateParamName;
	public String getTemplateParamName() {
		return templateParamName;
	}

	@Column(name="ui_panel_id")
	private String uiPanelId;
	public String getUiPanelId() {
		return uiPanelId;
	}

	public void setUiPanelId(String uiPanelId) {
		this.uiPanelId = uiPanelId;
	}
	
	public void setTemplateParamName(String templateParamName) {
		this.templateParamName = templateParamName;
	}
	
	@Override
	public String toString() {
		return "ReportParameter [reportParameterId=" + reportParameterId
				+ ", report=" + report.getReportName() + ", templateParamName="
				+ templateParamName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((report == null) ? 0 : report.hashCode());
		result = prime
				* result
				+ ((reportParameterId == null) ? 0 : reportParameterId
						.hashCode());
		result = prime
				* result
				+ ((templateParamName == null) ? 0 : templateParamName
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportParameter other = (ReportParameter) obj;
		if (report == null) {
			if (other.report != null)
				return false;
		} else if (!report.equals(other.report))
			return false;
		if (reportParameterId == null) {
			if (other.reportParameterId != null)
				return false;
		} else if (!reportParameterId.equals(other.reportParameterId))
			return false;
		if (templateParamName == null) {
			if (other.templateParamName != null)
				return false;
		} else if (!templateParamName.equals(other.templateParamName))
			return false;
		return true;
	}

	
}
