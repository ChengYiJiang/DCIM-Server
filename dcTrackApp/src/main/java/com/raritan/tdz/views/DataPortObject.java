package com.raritan.tdz.views;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.dto.PortConnectorDTO;

public class DataPortObject {
	
	public DataPortObject()
	{
	
	}
	
	private Long itemId;
	private Long portId;
	private boolean used;
	private int sortOrder;	
	private String portName;
	private String itemName;
	private String ipAddress;
	private String ipv6Address;
	private String macAddress;
	private String groupingVlanTag;
	private String speedLkuDesc;
	private String mediaLksDesc;
	private String protocolLkuDesc;
	private String connectorName;
	private String colorLkuDesc;
	private String colorNumber;
	private String vlanLkuDesc;
	private Long itemClassLksValueCode;
	private Long locationId;
	private Long portSubClassLksValueCode;
	private String subnet;
	private PortConnectorDTO connector;
	private int placementX;
	private int placementY;
	private Long faceValueCode;

	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public Long getPortId() {
		return portId;
	}
	public void setPortId(Long portId) {
		this.portId = portId;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public int getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getIpv6Address() {
		return ipv6Address;
	}
	public void setIpv6Address(String ipv6Address) {
		this.ipv6Address = ipv6Address;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getGroupingVlanTag() {
		return groupingVlanTag;
	}
	public void setGroupingVlanTag(String groupingVlanTag) {
		this.groupingVlanTag = groupingVlanTag;
	}
	public String getSpeedLkuDesc() {
		return speedLkuDesc;
	}
	public void setSpeedLkuDesc(String speedLkuDesc) {
		this.speedLkuDesc = speedLkuDesc;
	}
	public String getMediaLksDesc() {
		return mediaLksDesc;
	}
	public void setMediaLksDesc(String mediaLksDesc) {
		this.mediaLksDesc = mediaLksDesc;
	}
	public String getProtocolLkuDesc() {
		return protocolLkuDesc;
	}
	public void setProtocolLkuDesc(String protocolLkuDesc) {
		this.protocolLkuDesc = protocolLkuDesc;
	}
	public String getConnectorName() {
		return connectorName;
	}
	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}
	public String getColorLkuDesc() {
		return colorLkuDesc;
	}
	public void setColorLkuDesc(String colorLkuDesc) {
		this.colorLkuDesc = colorLkuDesc;
	}
	public String getColorNumber() {
		return colorNumber;
	}
	public void setColorNumber(String colorNumber) {
		this.colorNumber = colorNumber;
	}
	public String getVlanLkuDesc() {
		return vlanLkuDesc;
	}
	public void setVlanLkuDesc(String vlanLkuDesc) {
		this.vlanLkuDesc = vlanLkuDesc;
	}
	public void setItemClassLksValueCode(Long itemClassLksValueCode) {
		this.itemClassLksValueCode = itemClassLksValueCode;
	}
	public Long getItemClassLksValueCode() {
		return itemClassLksValueCode;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	public Long getLocationId() {
		return locationId;
	}
	
	public Long getPortSubClassLksValueCode() {
		return portSubClassLksValueCode;
	}
	public void setPortSubClassLksValueCode(Long portSubClassLksValueCode) {
		this.portSubClassLksValueCode = portSubClassLksValueCode;
	}
	public String getSubnet() {
		return subnet;
	}
	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}
	
	public PortConnectorDTO getConnector() {
		return connector;
	}
	public void setConnector(PortConnectorDTO connector) {
		this.connector = connector;
	}
	public int getPlacementX() {
		return placementX;
	}
	public void setPlacementX(int placementX) {
		this.placementX = placementX;
	}
	
	public int getPlacementY() {
		return placementY;
	}
	public void setPlacementY(int placementY) {
		this.placementY = placementY;
	}
	public Long getFaceValueCode() {
		return faceValueCode;
	}
	public void setFaceValueCode(Long faceValueCode) {
		this.faceValueCode = faceValueCode;
	}	
		
}
