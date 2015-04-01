package com.raritan.tdz.dto;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class DataPortDTO extends PortDTOBase implements DataPortInterface {
	
	private String ipAddress;
	private String ipv6Address;
	private String macAddress;
	private String speedLkuDesc;
	
	private String mediaLksDesc;
	private String protocolLkuDesc;
	private String location;
	
	private String vlanLkuDesc;
	private String subnet;
	
	private String communityString;
	private Long speedLkuId;
	private Long protocolLkuId;
	private Long mediaLksValueCode;
	private Long vlanLkuId;
	private DataPortInterface linkPort;
	private String ipAddressImport;
	private String proxyIndex;
	
	
	public DataPortDTO(){
		initDirtyFlagMap(this.getClass());
	}
	
	@Override
	@JsonProperty("ipAddress")
	public String getIpAddress() {
		return ipAddress;
	}

	@Override
	@JsonProperty("ipAddress")
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public String getIpv6Address() {
		return ipv6Address;
	}
	@Override
	public void setIpv6Address(String ipv6Address) {
		this.ipv6Address = ipv6Address;
	}
	@Override
	@JsonProperty("macAddress")
	public String getMacAddress() {
		return macAddress;
	}
	@Override
	@JsonProperty("macAddress")
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	@Override
	@JsonProperty("dataRate")
	public String getSpeedLkuDesc() {
		return speedLkuDesc;
	}
	@Override
	@JsonProperty("dataRate")
	public void setSpeedLkuDesc(String speedLkuDesc) {
		this.speedLkuDesc = speedLkuDesc;
	}
	@Override
	@JsonProperty("media")
	public String getMediaLksDesc() {
		return mediaLksDesc;
	}
	@Override
	@JsonProperty("media")
	public void setMediaLksDesc(String mediaLksDesc) {
		this.mediaLksDesc = mediaLksDesc;
	}
	@Override
	@JsonProperty("protocol")
	public String getProtocolLkuDesc() {
		return protocolLkuDesc;
	}
	@Override
	@JsonProperty("protocol")
	public void setProtocolLkuDesc(String protocolLkuDesc) {
		this.protocolLkuDesc = protocolLkuDesc;
	}
	@Override
	@JsonProperty("groupVLAN")
	public String getVlanLkuDesc() {
		return vlanLkuDesc;
	}
	@Override
	@JsonProperty("groupVLAN")
	public void setVlanLkuDesc(String vlanLkuDesc) {
		this.vlanLkuDesc = vlanLkuDesc;
	}
	@Override
	public String getSubnet() {
		return subnet;
	}
	@Override
	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}
	@Override
	@JsonProperty("snmpCommunity")
	public String getCommunityString() {
		return communityString;
	}
	@Override
	@JsonProperty("snmpCommunity")
	public void setCommunityString(String communityString) {
		this.communityString = communityString;
	}
	@Override
	public Long getSpeedLkuId() {
		return speedLkuId;
	}
	@Override
	public void setSpeedLkuId(Long speedLkuId) {
		this.speedLkuId = speedLkuId;
	}
	@Override
	public Long getProtocolLkuId() {
		return protocolLkuId;
	}
	@Override
	public void setProtocolLkuId(Long protocolLkuId) {
		this.protocolLkuId = protocolLkuId;
	}
	@Override
	public Long getMediaLksValueCode() {
		return mediaLksValueCode;
	}
	@Override
	public void setMediaLksValueCode(Long mediaLksValueCode) {
		this.mediaLksValueCode = mediaLksValueCode;
	}
	@Override
	public Long getVlanLkuId() {
		return vlanLkuId;
	}
	@Override
	public void setVlanLkuId(Long vlanLkuId) {
		this.vlanLkuId = vlanLkuId;
	}
	@Override
	public DataPortInterface getLinkPort() {
		return linkPort;
	}
	@Override
	public void setLinkPort(DataPortInterface linkPort) {
		this.linkPort = linkPort;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getIpAddressImport() {
		return ipAddressImport;
	}

	public void setIpAddressImport(String ipAddressImport) {
		this.ipAddressImport = ipAddressImport;
	}

	public String getProxyIndex() {
		return proxyIndex;
	}

	public void setProxyIndex(String proxyIndex) {
		this.proxyIndex = proxyIndex;
	}

	
}
