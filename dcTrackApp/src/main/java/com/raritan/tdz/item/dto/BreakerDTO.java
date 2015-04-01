package com.raritan.tdz.item.dto;

public class BreakerDTO {

	// default sort order column location and breaker column
	// used and phase, if phase is null return all phases.
	
	Long fpduId;                   // floor PDU id
	String fpduName;               // floor PDU name
	Long upsBankId;                // ups bank id
	String upsBankName;            // ups bank name	
	Long locationId;               // site id
	String location;               // site code
	Long powerPanelId;             // power panel id
	String powerPanelName;         // power panel name
	Long breakerId;                // breaker port id
	String breakerName;            // breaker port name
	Double ampsNameplate;          // power port ams_nameplate + volts_lks_id
	String outputWiringDesc;       // output wiring description
	Long outputWiringLkpValueCode; // output wiring lkpValue code
	String lineVolts;              // line volts
	String uiBreakerColumn;        // breaker column displayed on UI
	String uiRatingColumn;         // rating column displayed on UI

	public Long getFpduId() {
		return fpduId;
	}
	public void setFpduId(Long fpduId) {
		this.fpduId = fpduId;
	}
	public String getFpduName() {
		return fpduName;
	}
	public void setFpduName(String fpduName) {
		this.fpduName = fpduName;
	}
	public Long getUpsBankId() {
		return upsBankId;
	}
	public void setUpsBankId(Long upsBankId) {
		this.upsBankId = upsBankId;
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
	public Long getPowerPanelId() {
		return powerPanelId;
	}
	public void setPowerPanelId(Long powerPanelId) {
		this.powerPanelId = powerPanelId;
	}
	public String getPowerPanelName() {
		return powerPanelName;
	}
	public void setPowerPanelName(String powerPanelName) {
		this.powerPanelName = powerPanelName;
	}
	public Long getBreakerId() {
		return breakerId;
	}
	public void setBreakerId(Long breakerId) {
		this.breakerId = breakerId;
	}
	public String getBreakerName() {
		return breakerName;
	}
	public void setBreakerName(String breakerName) {
		this.breakerName = breakerName;
	}
	public Double getAmpsNameplate() {
		return ampsNameplate;
	}
	public void setAmpsNameplate(Double ampsNameplate) {
		this.ampsNameplate = ampsNameplate;
	}
	public String getOutputWiringDesc() {
		return outputWiringDesc;
	}
	public void setOutputWiringDesc(String outputWiringDesc) {
		this.outputWiringDesc = outputWiringDesc;
	}
	public Long getOutputWiringLkpValueCode() {
		return outputWiringLkpValueCode;
	}
	public void setOutputWiringLkpValueCode(Long outputWiringLkpValueCode) {
		this.outputWiringLkpValueCode = outputWiringLkpValueCode;
	}
	public String getLineVolts() {
		return lineVolts;
	}
	public void setLineVolts(String lineVolts) {
		this.lineVolts = lineVolts;
	}
	public String getUiBreakerColumn() {
		return uiBreakerColumn;
	}
	public void setUiBreakerColumn(String uiBreakerColumn) {
		this.uiBreakerColumn = uiBreakerColumn;
	}
	public String getUiRatingColumn() {
		return uiRatingColumn;
	}
	public void setUiRatingColumn(String uiRatingColumn) {
		this.uiRatingColumn = uiRatingColumn;
	}
	
}
