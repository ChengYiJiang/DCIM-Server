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

import com.raritan.tdz.domain.LksData;

@Entity
@Table(name="dct_report_config_details")
public class ReportConfigDetails  implements Serializable{

	private static final long serialVersionUID = -2557381429831562860L;

	public ReportConfigDetails(){}

	public ReportConfigDetails(long reportConfigDetailId,
			ReportConfig reportConfig, String fieldName,
			String fieldValue, Boolean isParameter, LksData conditionLookup,
			String uiPanelId) {
		super();
		this.reportConfigDetailId = reportConfigDetailId;
		this.reportConfig = reportConfig;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.isParameter = isParameter;
		this.conditionLookup = conditionLookup;
		this.uiPanelId = uiPanelId;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="report_config_details_seq")
	@SequenceGenerator(name="report_config_details_seq", sequenceName="dct_report_config_details_report_config_detail_id_seq", allocationSize=1)
	@Column(name="report_config_detail_id")
	private Long reportConfigDetailId;
	public Long getReportConfigDetailId() {
		return reportConfigDetailId;
	}

	public void setReportConfigDetailId(Long reportConfigDetailId) {
		this.reportConfigDetailId = reportConfigDetailId;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=ReportConfig.class)
	@JoinColumn(name="report_config_id")
	private ReportConfig reportConfig;
	public ReportConfig getReportConfig() {
		return reportConfig;
	}

	public void setReportConfig(ReportConfig reportConfig) {
		this.reportConfig = reportConfig;
	}
	
	@Column(name="field_name")
	private String fieldName;
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Column(name="field_value")
	private String fieldValue;
	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
	
	@Column(name="is_parameter")
	private Boolean isParameter;
	public Boolean getIsParameter() {
		return isParameter;
	}

	public void setIsParameter(Boolean isParameter) {
		this.isParameter = isParameter;
	}	
	
	@ManyToOne(fetch=FetchType.EAGER,targetEntity=LksData.class)
	@JoinColumn(name="condition_lks_id")
	private LksData conditionLookup;
	public LksData getConditionLookup() {
		return conditionLookup;
	}

	public void setConditionLookup(LksData conditionLookup) {
		this.conditionLookup = conditionLookup;
	}	
	
	@Column(name="ui_panel_id")
	private String uiPanelId;
	public String getUiPanelId() {
		return uiPanelId;
	}

	public void setUiPanelId(String uiPanelId) {
		this.uiPanelId = uiPanelId;
	}

	@Override
	public String toString() {
		return "ReportConfigDetails [reportConfigDetailId=" + reportConfigDetailId
				+ ", reportConfig=" + reportConfig + ", fieldName="
				+ fieldName + ", fieldValue=" + fieldValue + ", isParameter="
				+ isParameter + ", conditionLookup=" + conditionLookup
				+ ", uiPanelId=" + uiPanelId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result
				+ ((fieldValue == null) ? 0 : fieldValue.hashCode());
		result = prime * result
				+ ((isParameter == null) ? 0 : isParameter.hashCode());
		result = prime
				* result
				+ ((reportConfig == null) ? 0 : reportConfig
						.hashCode());
		result = prime * result
				+ ((reportConfigDetailId == null) ? 0 : reportConfigDetailId.hashCode());
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
		ReportConfigDetails other = (ReportConfigDetails) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (fieldValue == null) {
			if (other.fieldValue != null)
				return false;
		} else if (!fieldValue.equals(other.fieldValue))
			return false;
		if (isParameter == null) {
			if (other.isParameter != null)
				return false;
		} else if (!isParameter.equals(other.isParameter))
			return false;
		if (reportConfig == null) {
			if (other.reportConfig != null)
				return false;
		} else if (!reportConfig.equals(other.reportConfig))
			return false;
		if (reportConfigDetailId == null) {
			if (other.reportConfigDetailId != null)
				return false;
		} else if (!reportConfigDetailId.equals(other.reportConfigDetailId))
			return false;
		return true;
	}
	
	
}

