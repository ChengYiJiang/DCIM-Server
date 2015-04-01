package com.raritan.tdz.page.dto;

public class ColumnGroupDTO implements java.io.Serializable {

	private static final long serialVersionUID = 1935826780182564881L;

	private String groupName;

	private String fieldName;

	private String fieldLabel;

	private boolean fixedColumn;
	
	public boolean customField;

	/** This field is for generating the repeat custom field sql in condition. */
	private String customIds = "";
	
	public String getCustomIds() {
		return customIds;
	}

	public void setCustomIds(String customIds) {
		this.customIds = customIds;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}
	
	public boolean isFixedColumn() {
		return fixedColumn;
	}

	public void setFixedColumn(boolean fixedColumn) {
		this.fixedColumn = fixedColumn;
	}

	public boolean isCustomField() {
		return customField;
	}

	public void setCustomField(boolean customField) {
		this.customField = customField;
	}

}
