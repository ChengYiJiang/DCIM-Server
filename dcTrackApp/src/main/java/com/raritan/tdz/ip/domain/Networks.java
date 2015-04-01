package com.raritan.tdz.ip.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

import com.raritan.tdz.domain.DataCenterLocationDetails;

/**
 * Domain object representing Networks.
 */
@Entity
@Table(name = "`tblnetworks`")
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class Networks implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public Networks(){
	}
	
	@JsonProperty("id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@JsonProperty("locationId")
	public Long getSiteId() {
		return dataCenterLocation != null ? dataCenterLocation.getDataCenterLocationId() : null;
	}

	@JsonProperty("subnetPrefix")
	public String getNetworkSuffix() {
		return networkSuffix;
	}
	public void setNetworkSuffix(String networkSuffix) {
		this.networkSuffix = networkSuffix;
	}
	@JsonProperty("maskId")
	public Long getMaskId() {
		return mask != null ? mask.getId() : null ;
	}
	@JsonProperty("subnetStart")
	public String getSubnet() {
		return subnet;
	}
	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}
	@JsonProperty("subnetEnd")
	public String getSubnetEnd() {
		return subnetEnd;
	}
	public void setSubnetEnd(String subnetEnd) {
		this.subnetEnd = subnetEnd;
	}
	@JsonProperty("gateway")
	public String getGateway() {
		return gateway;
	}
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	@JsonProperty("isManaged")
	public Boolean getIsManaged() {
		return isManaged;
	}

	public void setIsManaged(Boolean isManaged) {
		this.isManaged = isManaged;
	}

	public DataCenterLocationDetails getDataCenterLocation() {
		return dataCenterLocation;
	}

	public void setDataCenterLocation(DataCenterLocationDetails dataCenterLocation) {
		this.dataCenterLocation = dataCenterLocation;
	}

	public NetMask getMask() {
		return mask;
	}

	public void setMask(NetMask mask) {
		this.mask = mask;
	}

	@JsonProperty("hostnameSuffix")
	public String getDnsSuffix() {
		return dnsSuffix;
	}

	public void setDnsSuffix(String dnsSuffix) {
		this.dnsSuffix = dnsSuffix;
	}

	@JsonProperty("subnetName")
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@JsonProperty("vlanNumber")
	public Long getVlanNumber() {
		return vlanNumber;
	}

	public void setVlanNumber(Long vlanNumber) {
		this.vlanNumber = vlanNumber;
	}
	@Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="tblnetworks_id_seq")
	@SequenceGenerator(name="tblnetworks_id_seq", sequenceName="tblnetworks_id_seq", allocationSize=1)
	private Long id;
	
	@ManyToOne(fetch=FetchType.EAGER,targetEntity=DataCenterLocationDetails.class)
	@JoinColumn(name="`siteid`")
	private DataCenterLocationDetails dataCenterLocation;
	
	@Column(name="`networksuffix`")
	private String networkSuffix;
	
	@ManyToOne(fetch=FetchType.EAGER,targetEntity=NetMask.class)
    @JoinColumn(name="`maskid`")
	private NetMask mask;
	
	@Column(name="`subnet`")
	private String subnet;
	
	
	@Column(name="`subnetend`")
	private String subnetEnd;
	
	@Column(name="`gateway`")
	private String gateway;

	@Column(name="dnssuffix")
	private String dnsSuffix;
	
	@Column(name="fullname")
	private String fullName;
	
	@Column(name="vlanNumber")
	private Long vlanNumber;
	
	@Transient
	private Boolean isManaged;


}
