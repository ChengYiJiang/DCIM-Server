package com.raritan.tdz.field.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raritan.tdz.domain.LksData;

@Entity
@Table(name="`dct_field_details`")
public class FieldDetails implements Serializable {
	
	private static final long serialVersionUID = -2862919870449144270L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_field_details_id_seq")
	@SequenceGenerator(name="dct_field_details_id_seq", sequenceName="dct_field_details_field_details_id_seq", allocationSize=1)
	@Column(name="`field_detail_id`")
	private Long fieldDetailId;
	
	@ManyToOne(fetch=FetchType.EAGER,targetEntity=Fields.class)
	@JoinColumn(name="`field_id`", nullable=false)
	private Fields field;
	
	@OneToOne(fetch=FetchType.EAGER,targetEntity=LksData.class)
	@JoinColumn(name="`class_lks_id`")
	private LksData classLks;
	
	@Column(name = "`display_name`")
	private String displayName;

	@Column(name = "`is_required_at_save`")
	private Boolean isRequiedAtSave;
	
	@Column(name = "`is_configurable`")
	private Boolean isConfigurable;

	public FieldDetails() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FieldDetails(Long fieldDetailId, Fields field, LksData classLks,
			String displayName, Boolean isRequiedAtSave) {
		super();
		this.fieldDetailId = fieldDetailId;
		this.field = field;
		this.classLks = classLks;
		this.displayName = displayName;
		this.isRequiedAtSave = isRequiedAtSave;
	}

	public Long getFieldDetailId() {
		return fieldDetailId;
	}

	public void setFieldDetailId(Long fieldDetailId) {
		this.fieldDetailId = fieldDetailId;
	}

	public Fields getField() {
		return field;
	}

	public void setField(Fields field) {
		this.field = field;
	}

	public LksData getClassLks() {
		return classLks;
	}

	public void setClassLks(LksData classLks) {
		this.classLks = classLks;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Boolean getIsRequiedAtSave() {
		return isRequiedAtSave;
	}

	public void setIsRequiedAtSave(Boolean isRequiedAtSave) {
		this.isRequiedAtSave = isRequiedAtSave;
	}

	public Boolean getIsConfigurable() {
		return isConfigurable;
	}

	public void setIsConfigurable(Boolean isConfigurable) {
		this.isConfigurable = isConfigurable;
	}

	@Override
	public String toString() {
		return "FieldDetails [fieldDetailId=" + fieldDetailId + ", field="
				+ field + ", classLks=" + classLks + ", displayName="
				+ displayName + ", isRequiedAtSave=" + isRequiedAtSave
				+ ", isConfigurable=" + isConfigurable + "]";
	}

}
