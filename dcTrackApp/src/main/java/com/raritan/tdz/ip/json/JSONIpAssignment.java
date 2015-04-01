package com.raritan.tdz.ip.json;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class JSONIpAssignment {

	@JsonProperty("portId")
	public Long getPortId() {
		return portId;
	}
	public void setPortId(Long portId) {
		this.portId = portId;
	}
	@JsonProperty("itemId")
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	@JsonProperty("portName")
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	@JsonProperty("itemName")
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@JsonProperty("ipAddress")
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@JsonProperty("mask")
	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	@JsonProperty("cidr")
	public Long getCidr() {
		return cidr;
	}

	public void setCidr(Long cidr) {
		this.cidr = cidr;
	}

	@JsonProperty("gateway")
	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	@JsonProperty("dnsName")
	public String getDnsName() {
		return dnsName;
	}
	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	@JsonProperty("domainName")
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	@JsonProperty("comment")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@JsonProperty("isVirtual")
	public Boolean getIsVirtual() {
		return isVirtual;
	}


	public void setIsVirtual(Boolean isVirtual) {
		this.isVirtual = isVirtual;
	}
	
	
	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonProperty("domainId")
	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@JsonProperty("ipAddressId")
	public Long getIpaddressId() {
		return ipaddressId;
	}
	public void setIpaddressId(Long ipaddressId) {
		this.ipaddressId = ipaddressId;
	}
	
	@JsonProperty("subnetId")
	public Long getSubnetId() {
		return subnetId;
	}
	public void setSubnetId(Long subnetId) {
		this.subnetId = subnetId;
	}


	private Long id;
	private Long ipaddressId;
	private String ipAddress;
	private String mask;
	private Long cidr;
	private String gateway;
	private String dnsName;
	private String domainName;
	private Long domainId;
	private Boolean isVirtual;
	private String comment;
	private Long portId;
	private Long itemId;
	private String portName;
	private String itemName;
	private Long subnetId;

}
