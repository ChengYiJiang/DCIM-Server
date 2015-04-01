package com.raritan.tdz.reports.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="dct_reports")
public class Report  implements Serializable{
	private static final long serialVersionUID = -4711228276814408803L;

	public Report(){}


	public Report(long reportId, String reportName, String templateName,
			Timestamp creationDate, String description,
			Set<ReportParameter> reportParameters,
			Set<ReportConfig> reportConfig) {
		super();
		this.reportId = reportId;
		this.reportName = reportName;
		this.templateName = templateName;
		this.creationDate = creationDate;
		this.description = description;
		this.reportParameters = reportParameters;
		this.reportConfig = reportConfig;
	}


	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_reports_seq")
	@SequenceGenerator(name="dct_reports_seq", sequenceName="dct_reports_report_id_seq", allocationSize=1)
	@Column(name="`report_id`")
	private Long reportId;
	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	@Column(name="report_name")
	private String reportName;
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	@Column(name="template_name")
	private String templateName;
	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	@Column(name="creation_date")
	private Timestamp creationDate;
	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	
	@Column(name="description")
	private String description;
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "report", orphanRemoval = true, cascade={javax.persistence.CascadeType.ALL})
	private Set<ReportParameter> reportParameters = new HashSet<ReportParameter>(0);
	public Set<ReportParameter> getReportParameters() {
		return reportParameters;
	}

	public void setReportParameters(Set<ReportParameter> reportParameters) {
		this.reportParameters = reportParameters;
	}
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "report", orphanRemoval = true, cascade={javax.persistence.CascadeType.ALL})
	private Set<ReportConfig> reportConfig = new HashSet<ReportConfig>(0);
	public Set<ReportConfig> getReportConfig() {
		return reportConfig;
	}

	public void setReportConfig(Set<ReportConfig> reportConfig) {
		this.reportConfig = reportConfig;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((reportId == null) ? 0 : reportId.hashCode());
		result = prime * result
				+ ((reportName == null) ? 0 : reportName.hashCode());
		result = prime * result
				+ ((templateName == null) ? 0 : templateName.hashCode());
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
		Report other = (Report) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (reportId == null) {
			if (other.reportId != null)
				return false;
		} else if (!reportId.equals(other.reportId))
			return false;
		if (reportName == null) {
			if (other.reportName != null)
				return false;
		} else if (!reportName.equals(other.reportName))
			return false;
		if (templateName == null) {
			if (other.templateName != null)
				return false;
		} else if (!templateName.equals(other.templateName))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Report [reportId=" + reportId + ", reportName=" + reportName
				+ ", templateName=" + templateName + ", creationDate="
				+ creationDate + ", description=" + description
				+ ", reportParameters=" + reportParameters
				+ ", reportConfig=" + reportConfig + "]";
	}
	

	
}
