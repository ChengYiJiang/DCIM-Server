package com.raritan.tdz.page.dto;

public class FilterDTO implements java.io.Serializable {

	private static final long serialVersionUID = 3647924414465823721L;
	
	private int groupType;

	private String greaterThan;

	private String lessThan;

	private String equal;

	private boolean isLookup;
	
	private String lookupCodes;

	public int getGroupType() {
		return groupType;
	}
	
	public void setGroupType(int groupType) {
		this.groupType=groupType;
	}
	
	public String getGreaterThan() {
		return greaterThan;
	}
	
	public void setGreaterThan(String greaterThan) {
		this.greaterThan=greaterThan;
	}
	
	public String getLessThan() {
		return lessThan;
	}
	
	public void setLessThan(String lessThan) {
		this.lessThan=lessThan;
	}
	
	public String getEqual() {
		return equal;
	}
	
	public void setEqual(String equal) {
		this.equal=equal;
	}
	
	public boolean isLookup() {
		return isLookup;
	}
	
	public boolean getIsLookup() {
		return isLookup;
	}
	
	public void setIsLookup(boolean isLookup) {
		this.isLookup=isLookup;
	}
	
	public String getLookupCodes() {
		return lookupCodes;
	}
	
	public void setLookupCodes(String lookupCodes) {
		this.lookupCodes=lookupCodes;
	}
	
}
