/**
 * 
 */
package com.raritan.tdz.dto;

/**
 * @author prasanna
 * This represents a Data transfer object for checking
 * uniqueness of a specific parameter in the database
 */
public class UniqueValidatorDTO {
	private String uiId;
	private Object value;
	private String siteCode;
	private Long parentId;
	private String ignoreProperty;
	private Object ignorePropertyValue;	
			
	public String getUiId() {
		return uiId;
	}
	public void setUiId(String uiId) {
		this.uiId = uiId;
	}
	public Object getValue() {
		return value;
	}
	public String getIgnoreProperty() {
		return ignoreProperty;
	}
	public void setIgnoreProperty(String ignoreProperty) {
		this.ignoreProperty = ignoreProperty;
	}
	public Object getIgnorePropertyValue() {
		return ignorePropertyValue;
	}
	public void setIgnorePropertyValue(Object ignorePropertyValue) {
		this.ignorePropertyValue = ignorePropertyValue;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getSiteCode() {
		return siteCode;
	}
	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	
}
