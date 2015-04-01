package com.raritan.tdz.dctimport.dto;

/**
 * @author KC/bunty
 *
 */
public class DataPortImport extends DCTImportBase {

	@Header("itemlocation")
	private String itemLocation;
	
	@Header("itemname")
	private String itemName;
	
	@Header("portname")
	private String portName;		
	
	@Header("newportname")
	private String newPortName;
	
	@Header("index")
	private Integer index;
	
	@Header("colorcode")
	private String colorCode;
	
	@Header("grouping/vlan")
	private String groupingVLan;
	
	@Header("macaddress")
	private String macAddress;
	
	@Header("ipaddress")
	private String ipAddress;
	
	@Header("proxyindex")
	private String proxyIndex;
	
	@Header("snmpcommunity")
	private String snmpCommunity;
	
	@Header("comment")
	private String comment;
	
	@Header("connector")
	private String connector;
	
	@Header("media")
	private String media;								
	
	@Header("protocol")
	private String protocol;
	
	@Header("datarate")
	private String dataRate;
	
	@Header("porttype")
	private String portType;		
	
	public String getPortType() {
		return portType;
	}

	public void setPortType(String portType) {
		this.portType = portType;
	}

	
	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getDataRate() {
		return dataRate;
	}

	public void setDataRate(String dataRate) {
		this.dataRate = dataRate;
	}

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

	
	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
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

	public String getGroupingVLan() {
		return groupingVLan;
	}

	public void setGroupingVLan(String groupingVLan) {
		this.groupingVLan = groupingVLan;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getProxyIndex() {
		return proxyIndex;
	}

	public void setProxyIndex(String proxyIndex) {
		this.proxyIndex = proxyIndex;
	}

	public String getSnmpCommunity() {
		return snmpCommunity;
	}

	public void setSnmpCommunity(String snmpCommunity) {
		this.snmpCommunity = snmpCommunity;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "DataPortImport [itemLocation=" + itemLocation + ", itemName="
				+ itemName + ", portName=" + portName + ", newPortName="
				+ newPortName + ", index=" + index + ", colorCode=" + colorCode
				+ ", groupingVLan=" + groupingVLan + ", macAddress="
				+ macAddress + ", ipAddress=" + ipAddress + ", proxyIndex="
				+ proxyIndex + ", snmpCommunity=" + snmpCommunity
				+ ", comment=" + comment + ", connector=" + connector
				+ ", media=" + media + ", protocol=" + protocol + ", dataRate="
				+ dataRate + ", portType=" + portType + "]";
	}
	
	
	
}
