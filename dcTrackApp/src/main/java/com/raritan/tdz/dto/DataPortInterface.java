package com.raritan.tdz.dto;

public interface DataPortInterface extends PortInterface {

	public abstract String getIpAddress();
	public abstract void setIpAddress(String ipAddress);

	public abstract String getIpv6Address();
	public abstract void setIpv6Address(String ipv6Address);

	public abstract String getMacAddress();
	public abstract void setMacAddress(String macAddress);

	public abstract Long getVlanLkuId();
	public abstract void setVlanLkuId(Long vlanLkuId);

	public abstract String getSpeedLkuDesc();
	public abstract void setSpeedLkuDesc(String speedLkuDesc);
	
	public abstract Long getSpeedLkuId();
	public abstract void setSpeedLkuId(Long speedLkuId);

	public abstract String getMediaLksDesc();
	public abstract void setMediaLksDesc(String mediaLksDesc);
	
	public abstract Long getMediaLksValueCode();
	public abstract void setMediaLksValueCode(Long mediaLookupValueCode);

	public abstract String getProtocolLkuDesc();
	public abstract void setProtocolLkuDesc(String protocolLkuDesc);
	
	public abstract Long getProtocolLkuId();
	public abstract void setProtocolLkuId(Long protocolLkuId);

	public abstract String getVlanLkuDesc();
	public abstract void setVlanLkuDesc(String vlanLkuDesc);

	public abstract String getSubnet();
	public abstract void setSubnet(String subnet);	
	
	public abstract String getCommunityString();
	public abstract void setCommunityString(String communityString);
	
	public abstract DataPortInterface getLinkPort();
	public abstract void setLinkPort(DataPortInterface linkPort);

	public abstract String getIpAddressImport();
	public abstract void setIpAddressImport(String ipAddressImport);
	public abstract String getProxyIndex();
	public abstract void setProxyIndex(String proxyIndex);
	
}
