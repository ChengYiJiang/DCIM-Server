package com.raritan.tdz.field.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raritan.tdz.domain.LkuData;


@Entity
@Table(name="`dct_fields`")
public class Fields implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 857995942135363357L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_fields_id_seq")
	@SequenceGenerator(name="dct_fields_id_seq", sequenceName="dct_fields_field_id_seq", allocationSize=1)
	@Column(name = "`field_id`")
	private Long fieldId;

	@OneToOne(fetch=FetchType.EAGER,targetEntity=LkuData.class)
	@JoinColumn(name="`custom_lku_id`", nullable=false)
	private LkuData customLku;
	
	@Column(name = "`ui_view_id`")
	private String uiViewId;

	@Column(name = "`ui_view_panel_id`")
	private String uiViewPanelId;

	@Column(name = "`ui_component_id`")
	private String uiComponentId;
	
	@Column(name = "`default_name`")
	private String defaultName;

	@Column(name = "`sort_order`")
	private int sortOrder;
	
	@Column(name = "`db_column_name`")
	private String dbColumnName;

	@Column(name = "`entity_attribute_name`")
	private String entityAttributeName;

	public Fields() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Fields(Long fieldId, String uiViewId, String uiComponentId, LkuData customLku,
			String defaultName, String dbColumnName, String entityAttributeName) {
		super();
		this.fieldId = fieldId;
		this.uiViewId = uiViewId;
		this.uiComponentId = uiComponentId;
		this.customLku = customLku;
		this.defaultName = defaultName;
		this.dbColumnName = dbColumnName;
		this.entityAttributeName = entityAttributeName;
	}

	public String getUiViewId() {
		return uiViewId;
	}

	public void setUiViewId(String uiViewId) {
		this.uiViewId = uiViewId;
	}
	
	public String getUiViewPanelId() {
		return uiViewPanelId;
	}

	public void setUiViewPanelId(String uiViewPanelId) {
		this.uiViewPanelId = uiViewPanelId;
	}

	public String getUiComponentId() {
		return uiComponentId;
	}

	public void setUiComponentId(String uiComponentId) {
		this.uiComponentId = uiComponentId;
	}

	public LkuData getCustomLku() {
		return customLku;
	}

	public void setCustomLku(LkuData customLku) {
		this.customLku = customLku;
	}

	public Long getFieldId() {
		return fieldId;
	}

	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getDbColumnName() {
		return dbColumnName;
	}

	public void setDbColumnName(String dbColumnName) {
		this.dbColumnName = dbColumnName;
	}
	
	public String getEntityAttributeName() {
		return entityAttributeName;
	}

	public void setEntityAttributeName(String entityAttributeName) {
		this.entityAttributeName = entityAttributeName;
	}

	@Override
	public String toString() {
		return "Fields [fieldId=" + fieldId + ", customLku=" + customLku
				+ ", uiViewId=" + uiViewId + ", uiViewPanelId=" + uiViewPanelId
				+ ", uiComponentId=" + uiComponentId 
				+ ", defaultName=" + defaultName 
				+ ", sortOrder=" + sortOrder
				+ ", dbColumnName=" + dbColumnName
				+ ", entityAttributeName=" + entityAttributeName + "]";
	}

}
