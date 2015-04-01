package com.raritan.tdz.field.dto;

public class FieldDTO {
	/* these 2 are required to reach a specific record 
	 * in the field and field_details table */
	private String uiComponentId;
	private Long classLkuId; // for custom (tiCustomField1)
	private Long classLksId; // for fixed fields (tiName, tiSerialNumber)

	private Boolean isRequired;
	String displayName;
	String uiViewId; 
	
	public Long getClassLkuId() {
		return classLkuId;
	}

	public void setClassLkuId(Long classLkuId) {
		this.classLkuId = classLkuId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getUiViewId() {
		return uiViewId;
	}

	public void setUiViewId(String uiPage) {
		this.uiViewId = uiPage;
	}

	public String getUiComponentId() {
		return uiComponentId;
	}

	public void setUiComponentId(String uiComponentId) {
		this.uiComponentId = uiComponentId;
	}

	public Long getClassLksId() {
		return classLksId;
	}

	public void setClassLksId(Long classLksId) {
		this.classLksId = classLksId;
	}

	public Boolean getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(Boolean isRequired) {
		this.isRequired = isRequired;
	}
}
