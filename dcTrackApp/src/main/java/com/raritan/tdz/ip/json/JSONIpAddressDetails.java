package com.raritan.tdz.ip.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;


@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class JSONIpAddressDetails{
	   
    private class DataPortInfo{
    	public Long getPortId() {
			return portId;
		}
		public void setPortId(Long portId) {
			this.portId = portId;
		}
		public Long getItemId() {
			return itemId;
		}
		public void setItemId(Long itemId) {
			this.itemId = itemId;
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
		Long portId;
    	Long itemId;
    	String portName;
    	String itemName;
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
	@JsonProperty("isDuplicateIpAllowed")
	public Boolean getIsDuplicateIpAllowed() {
		return isDuplicateIpAllowed;
	}
	

	public void setIsDuplicateIpAllowed(Boolean isDuplicateIpAllowed) {
		this.isDuplicateIpAllowed = isDuplicateIpAllowed;
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

	public List<DataPortInfo> getDataPort() {
		return dataPorts;
	}

	@JsonProperty("isIpBeingGatewayAllowed")
	public Boolean getIsIpBeingGatewayAllowed() {
		return isIpBeingGatewayAllowed;
	}

	public void setIsIpBeingGatewayAllowed(Boolean isIpBeingGatewayAllowed) {
		this.isIpBeingGatewayAllowed = isIpBeingGatewayAllowed;
	}

	public void setDataPorts(List<DataPortInfo> dataPorts){
		this.dataPorts = dataPorts;
	}
	
	public void setDataPortsFromDomain(Set<DataPort> dataPortsIn){
		this.dataPorts = new ArrayList<DataPortInfo>();
		if (dataPortsIn != null && dataPortsIn.size() > 0){
			for( DataPort dp : dataPortsIn ){
				DataPortInfo dpInfo = new DataPortInfo();
				Item i = dp.getItem();
				if( i != null ){
					dpInfo.setItemId(i.getItemId());
					dpInfo.setItemName(i.getItemName());
				}
				dpInfo.setPortId(dp.getPortId());
				dpInfo.setPortName(dp.getPortName());
				this.dataPorts.add(dpInfo);
			}
		}
	}
	
	@JsonProperty("subnetId")
	public Integer getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(Integer subnetId) {
		this.subnetId = subnetId;
	}


	private Long id;
	
	@JsonProperty("dataPorts")
	private List<DataPortInfo> dataPorts;
	
	private String ipAddress;
	private String mask;
	private Long cidr;
	private String gateway;
	private String dnsName;
	private String domainName;
	private Long domainId;
	private Boolean isVirtual;
	private String comment;
	private Boolean isDuplicateIpAllowed;
	private Boolean isIpBeingGatewayAllowed;
	private Integer subnetId;
}
