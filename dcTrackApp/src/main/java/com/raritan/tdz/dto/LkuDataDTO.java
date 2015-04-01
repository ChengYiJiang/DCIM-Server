package com.raritan.tdz.dto;

public class LkuDataDTO {
	private Long lkuId;
	private String lkuTypeName;
	private String lkuAttribute;
	private String lkuValue;
	private Long lksId;
	private int sortOrder;
	  
	public LkuDataDTO(){
		
	}
	
	public Long getLkuId() {
		return lkuId;
	}
	public void setLkuId(Long lkuId) {
		this.lkuId = lkuId;
	}
	public String getLkuTypeName() {
		return lkuTypeName;
	}
	public void setLkuTypeName(String lkuTypeName) {
		this.lkuTypeName = lkuTypeName;
	}
	public String getLkuAttribute() {
		return lkuAttribute;
	}
	public void setLkuAttribute(String lkuAttribute) {
		this.lkuAttribute = lkuAttribute;
	}
	public String getLkuValue() {
		return lkuValue;
	}
	public void setLkuValue(String lkuValue) {
		this.lkuValue = lkuValue;
	}
	public Long getLksId() {
		return lksId;
	}
	public void setLksId(Long lksId) {
		this.lksId = lksId;
	}
	public int getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	  
	  
}
