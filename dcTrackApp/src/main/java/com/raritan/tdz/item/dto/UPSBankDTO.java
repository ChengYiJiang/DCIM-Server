package com.raritan.tdz.item.dto;

public class UPSBankDTO {
	Long upsBankId; 
	String upsBankName;
	Long locationId; //site id
	String location; //site code
	Long capacity; // ratingKva
	Long volts; //ratingV
	Long outputWiringLkpValueCode; 
	String outputWiringDesc; //lkp_value_code=7022 -> "3-Wire + Ground"
						 //lkp_value_code=7023 -> "4-Wire + Ground"

	public Long getOutputWiringLkpValueCode() {
		return outputWiringLkpValueCode;
	}
	public void setOutputWiringLkpValueCode(Long outputWiringLkpValueCode) {
		this.outputWiringLkpValueCode = outputWiringLkpValueCode;
	}

	public String getUpsBankName() {
		return upsBankName;
	}
	public void setUpsBankName(String upsBankName) {
		this.upsBankName = upsBankName;
	}
	public Long getLocationId() {
		return locationId;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Long getCapacity() {
		return capacity;
	}
	public void setCapacity(Long capacity) {
		this.capacity = capacity;
	}
	public Long getVolts() {
		return volts;
	}
	public void setVolts(Long volts) {
		this.volts = volts;
	}
	public String getOutputWiringDesc() {
		return outputWiringDesc;
	}
	public void setOutputWiringDesc(String outputWiring) {
		this.outputWiringDesc = outputWiring;
	}
	public Long getUpsBankId() {
		return upsBankId;
	}
	public void setUpsBankId(Long upsBankId) {
		this.upsBankId = upsBankId;
	}
}
