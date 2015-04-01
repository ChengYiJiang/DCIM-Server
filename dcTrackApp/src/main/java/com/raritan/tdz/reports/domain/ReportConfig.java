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

import com.raritan.tdz.domain.Reservations;

@Entity
@Table(name="dct_report_configs")
public class ReportConfig  implements Serializable{
	private static final long serialVersionUID = -4711228276814408803L;

	public ReportConfig(){}


	public ReportConfig(long reportConfigId, Report report,
			String configName, Long userId, Timestamp creationDate,
			Set<ReportConfigDetails> reportConfigDetails) {
		super();
		this.reportConfigId = reportConfigId;
		this.report = report;
		this.configName = configName;
		this.userId = userId;
		this.creationDate = creationDate;
		this.reportConfigDetails = reportConfigDetails;
	}


	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_report_configs_seq")
	@SequenceGenerator(name="dct_report_configs_seq", sequenceName="dct_report_configs_report_config_id_seq", allocationSize=1)
	@Column(name="report_config_id")
	private Long reportConfigId;
	public Long getReportConfigId() {
		return reportConfigId;
	}

	public void setReportConfigId(Long reportConfigId) {
		this.reportConfigId = reportConfigId;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "report_id", nullable = false)
	private Report report;
	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}
	
	@Column(name="config_name")
	private String configName;
	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	@Column(name="user_id")
	private Long userId;
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	@Column(name="creation_date")
	private Timestamp creationDate;
	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "reportConfig", orphanRemoval = true, cascade={javax.persistence.CascadeType.ALL})
	private Set<ReportConfigDetails> reportConfigDetails = new HashSet<ReportConfigDetails>(0);
	public Set<ReportConfigDetails> getReportConfigDetails() {
		return reportConfigDetails;
	}

	public void setReportConfigDetails(Set<ReportConfigDetails> reportConfigDetails) {
		this.reportConfigDetails = reportConfigDetails;
	}

	@Override
	public String toString() {
		return "ReportConfig [reportConfigId=" + reportConfigId
				+ ", report=" + report + ", configName=" + configName
				+ ", userId=" + userId + ", creationDate=" + creationDate
				+ ", reportConfigDetails=" + reportConfigDetails + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configName == null) ? 0 : configName.hashCode());
		result = prime * result + ((report == null) ? 0 : report.hashCode());
		result = prime
				* result
				+ ((reportConfigId == null) ? 0 : reportConfigId
						.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		ReportConfig other = (ReportConfig) obj;
		if (configName == null) {
			if (other.configName != null)
				return false;
		} else if (!configName.equals(other.configName))
			return false;
		if (report == null) {
			if (other.report != null)
				return false;
		} else if (!report.equals(other.report))
			return false;
		if (reportConfigId == null) {
			if (other.reportConfigId != null)
				return false;
		} else if (!reportConfigId.equals(other.reportConfigId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	

}
