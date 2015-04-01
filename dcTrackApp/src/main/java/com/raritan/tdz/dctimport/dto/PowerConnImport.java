package com.raritan.tdz.dctimport.dto;

import org.springframework.validation.Errors;

/**
 * import file for power connection
 * @author Santo Rosario
 *
 */
public class PowerConnImport extends DCTImportBase {
	private Float circuitId;
	private Long statusLksValueCode;
	
	@Header(value="startingitemlocation")
	private String startingItemLocation;
	
	@Header(value="startingitemname")
	private String startingItemName;
	
	@Header(value="startingportname")
	private String startingPortName;

	@Header(value="cordtype")
	String cordType;
	
	@Header(value="cordid")
	String cordId;
	
	@Header(value="cordcolor")
	String cordColor;
	
	@Header(value="cordlength")
	String cordLength;
	
	@Header(value="endingitemlocation")
	private String endingItemLocation;
	
	@Header(value="endingitemname")
	private String endingItemName;
	
	@Header(value="endingportname")
	private String endingPortName;
	
	@Header(value="endingitemtype")
	private String endingItemType;

	public PowerConnImport() {
		super();
	}

	public String getStartingItemLocation() {
		return startingItemLocation;
	}

	public void setStartingItemLocation(String startingItemLocation) {
		this.startingItemLocation = startingItemLocation;
	}

	public String getStartingItemName() {
		return startingItemName;
	}

	public void setStartingItemName(String startingItemName) {
		this.startingItemName = startingItemName;
	}

	public String getStartingPortName() {
		return startingPortName;
	}

	public void setStartingPortName(String startingPortName) {
		this.startingPortName = startingPortName;
	}

	public String getCordType() {
		return cordType;
	}

	public void setCordType(String cordType) {
		this.cordType = cordType;
	}

	public String getCordId() {
		return cordId;
	}

	public void setCordId(String cordId) {
		this.cordId = cordId;
	}

	public String getCordColor() {
		return cordColor;
	}

	public void setCordColor(String cordColor) {
		this.cordColor = cordColor;
	}

	public String getCordLength() {
		return cordLength;
	}

	public void setCordLength(String cordLength) {
		this.cordLength = cordLength;
	}

	public String getEndingItemLocation() {
		return endingItemLocation;
	}

	public void setEndingItemLocation(String endingItemLocation) {
		this.endingItemLocation = endingItemLocation;
	}

	public String getEndingItemName() {
		return endingItemName;
	}

	public void setEndingItemName(String endingItemName) {
		this.endingItemName = endingItemName;
	}

	public String getEndingPortName() {
		return endingPortName;
	}

	public void setEndingPortName(String endingPortName) {
		this.endingPortName = endingPortName;
	}

	public Float getCircuitId() {
		return circuitId;
	}

	public void setCircuitId(Float circuitId) {
		this.circuitId = circuitId;
	}

	public Long getStatusLksValueCode() {
		return statusLksValueCode;
	}

	public void setStatusLksValueCode(Long statusLksValueCode) {
		this.statusLksValueCode = statusLksValueCode;
	}

	public String getEndingItemType() {
		return endingItemType;
	}

	public void setEndingItemType(String endingItemType) {
		this.endingItemType = endingItemType;
	}

	public boolean isVpcEndingPort() {
		
		if((endingItemName.equalsIgnoreCase("VPC-A") || endingItemName.equalsIgnoreCase("VPC-B")) 
				&& endingPortName.equalsIgnoreCase("VPP") 
				&& endingItemType.equalsIgnoreCase("Outlet")) {
			return true;
		}
		
		return false;
	}

	public String getVpcLabel() {
		String[] temp = endingItemName.toUpperCase().split("-");
		
		if(temp.length == 2) return temp[1];
		
		return endingItemName;
	}
	
	@Override
	public String toString() {
		return "PowerConnImport [circuitId=" + circuitId
				+ ", statusLksValueCode=" + statusLksValueCode
				+ ", startingItemLocation=" + startingItemLocation
				+ ", startingItemName=" + startingItemName
				+ ", startingPortName=" + startingPortName + ", cordType="
				+ cordType + ", cordId=" + cordId + ", cordColor=" + cordColor
				+ ", cordLength=" + cordLength + ", endingItemLocation="
				+ endingItemLocation + ", endingItemName=" + endingItemName
				+ ", endingPortName=" + endingPortName + ", endingItemType="
				+ endingItemType + "]";
	}

	private void validate(String fieldName, String displayValue, Errors errors) {
		if(fieldName == null || fieldName.trim().isEmpty()) {
			Object[] errorArgs = { displayValue };
			errors.reject("ItemValidator.fieldRequired", errorArgs, "Cannot find cord type");
		}
	}

	public void checkRequiredFields(Errors errors) {
		validate(startingItemLocation, "Starting Item Location", errors);
		validate(startingItemName, "Starting Item Name", errors);
		validate(startingPortName, "Starting Port Name", errors);
		validate(endingItemLocation, "Ending Item Location", errors);
		validate(endingItemName, "Ending Item Name", errors);
		validate(endingPortName, "Ending Port Name", errors);		
	}
}
