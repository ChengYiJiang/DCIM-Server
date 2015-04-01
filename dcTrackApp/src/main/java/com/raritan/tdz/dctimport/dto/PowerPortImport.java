package com.raritan.tdz.dctimport.dto;

/**
 * @author bunty
 *
 */
// operation, object, itemlocation, itemname, redundancy(foritem), portname, newportname, index, colorcode, connector, phasetype, volts, powerfactor, watts(n), watts(b), comment
public class PowerPortImport extends DCTImportBase {

	@Header("itemlocation")
	private String itemLocation;
	
	@Header("itemname")
	private String itemName;
	
	@Header("redundancy(foritem)")
	private String itemRedundancy;
	
	@Header("portname")
	private String portName;
	
	@Header("newportname")
	private String newPortName;
	
	@Header("porttype")
	private String portType;
	
	@Header("index")
	private Integer index;
	
	@Header("colorcode")
	private String colorCode;
	
	@Header("connector")
	private String connector;
	
	@Header("phasetype")
	private String phaseType;
	
	@Header("volts")
	private String volts;
	
	@Header("powerfactor")
	private Double powerFactor;
	
	@Header("watts(n)")
	private Long wattsNameplate;
	
	@Header("watts(b)")
	private String wattsBudget;
	
	@Header("comment")
	private String comment;

	public String getItemLocation() {
		return itemLocation;
	}

	public void setItemLocation(String itemLocation) {
		this.itemLocation = itemLocation;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemRedundancy() {
		return itemRedundancy;
	}

	public void setItemRedundancy(String itemRedundancy) {
		this.itemRedundancy = itemRedundancy;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getNewPortName() {
		return newPortName;
	}

	public void setNewPortName(String newPortName) {
		this.newPortName = newPortName;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getColorCode() {
		return colorCode;
	}

	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}

	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
	}

	public String getPhaseType() {
		return phaseType;
	}

	public void setPhaseType(String phaseType) {
		this.phaseType = phaseType;
	}

	public String getVolts() {
		return volts;
	}

	public void setVolts(String volts) {
		this.volts = volts;
	}

	public Double getPowerFactor() {
		return powerFactor;
	}

	public void setPowerFactor(Double powerFactor) {
		this.powerFactor = powerFactor;
	}

	public Long getWattsNameplate() {
		return wattsNameplate;
	}

	public void setWattsNameplate(Long wattsNameplate) {
		this.wattsNameplate = wattsNameplate;
	}

	public String getWattsBudget() {
		return wattsBudget;
	}

	public void setWattsBudget(String wattsBudget) {
		this.wattsBudget = wattsBudget;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getPortType() {
		return portType;
	}

	public void setPortType(String portType) {
		this.portType = portType;
	}

	@Override
	public String toString() {
		return "PowerPortImport [itemLocation=" + itemLocation + ", itemName="
				+ itemName + ", itemRedundancy=" + itemRedundancy
				+ ", portName=" + portName + ", newPortName=" + newPortName
				+ ", portType=" + portType + ", index=" + index
				+ ", colorCode=" + colorCode + ", connector=" + connector
				+ ", phaseType=" + phaseType + ", volts=" + volts
				+ ", powerFactor=" + powerFactor + ", wattsNameplate="
				+ wattsNameplate + ", wattsBudget=" + wattsBudget
				+ ", comment=" + comment + "]";
	}
	
	
	
}
