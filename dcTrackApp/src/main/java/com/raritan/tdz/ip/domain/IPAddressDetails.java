package com.raritan.tdz.ip.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.LkuData;


@Entity
@Table(name = "`tblipaddresses`")
public class IPAddressDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2268580963348597148L;

	public IPAddressDetails() {
	}

	public IPAddressDetails(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public IPAddressDetails( IPAddressDetails origIpAddress){
		this.id = origIpAddress.getId();
		this.ipAddress = origIpAddress.getIpAddress();

		this.mask = origIpAddress.getMask();		
		this.gateway = origIpAddress.getGateway();
		this.dnsName = origIpAddress.getDnsName();
		this.networkId = origIpAddress.getNetworkId();
		this.isVirtual = origIpAddress.getIsVirtual();
		this.comment = origIpAddress.getComment();
		this.domain = origIpAddress.getDomain();
		this.ipTeaming = origIpAddress.getIpTeaming();
	}

	public Object clone( ){
		IPAddressDetails clone = new IPAddressDetails();
		clone.id = this.id;
		if(this.getIpAddress() != null ){
			clone.ipAddress = new String(this.getIpAddress());
		}
		if( this.mask != null ){
			clone.mask = (NetMask)this.mask.clone();
		}
		if( this.getGateway() != null ){
			clone.gateway = new String(this.getGateway());
		}
		if(this.getDnsName() != null){
			clone.dnsName = new String(this.getDnsName());
		}
		if( this.getNetworkId() != null ){
			clone.networkId = new Long(this.getNetworkId());
		}
		if( this.getIsVirtual() != null ){
			clone.isVirtual = new Boolean(this.getIsVirtual());
		}
		if( this.getComment() != null ){
			clone.comment = new String(this.getComment());
		}
		if( this.domain != null ){
			clone.domain = (LkuData)this.domain.clone();
		}
		if( this.ipTeaming != null ){
			Set<IPTeaming> teams = new HashSet<IPTeaming>();
			for( IPTeaming t : ipTeaming ){
				IPTeaming cloneTeam = new IPTeaming();
				cloneTeam.setId(t.getId());
				cloneTeam.setDataPort(t.getDataPort());
				cloneTeam.setIpAddress(t.getIpAddress());
				teams.add(cloneTeam);
			}
			clone.setIpTeaming(teams);
		}

		return clone;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Set<DataPort> getDataPortsUsingIP() {
		Set<DataPort> dataPorts = new HashSet<DataPort>();
		if( ipTeaming != null && ipTeaming.size() > 0 ){
			for( IPTeaming t : ipTeaming ){
				dataPorts.add(t.getDataPort());
			}
		}
		return dataPorts;
	}


	public NetMask getMask() {
		return mask;
	}

	public void setMask(NetMask mask) {
		this.mask = mask;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getDnsName() {
		return dnsName;
	}

	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	public Boolean getIsVirtual() {
		return isVirtual;
	}

	public void setIsVirtual(Boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	public LkuData getDomain() {
		return domain;
	}

	public void setDomain(LkuData domain) {
		this.domain = domain;
	}

	public Long getNetworkId() {
		return networkId;
	}

	public void setNetworkId(Long networkId) {
		this.networkId = networkId;
	}

	public Boolean getIsDuplicatIpAllowed() {
		return isDuplicatIpAllowed;
	}

	public void setIsDuplicatIpAllowed(Boolean isDuplicatIpAllowed) {
		this.isDuplicatIpAllowed = isDuplicatIpAllowed;
	}
	public Boolean getIsIpBeingGatewayAllowed() {
		return isIpBeingGatewayAllowed;
	}

	public void setIsIpBeingGatewayAllowed(Boolean isIpBeingGatewayAllowed) {
		this.isIpBeingGatewayAllowed = isIpBeingGatewayAllowed;
	}

	public Boolean getIsTeamingAllowed() {
		return isTeamingAllowed;
	}

	public void setIsTeamingAllowed(Boolean isTeamingAllowed) {
		this.isTeamingAllowed = isTeamingAllowed;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;

		if (obj instanceof IPAddressDetails) {
			IPAddressDetails other = (IPAddressDetails)obj;
			if (this.getIpAddress() != null &&
					other.getIpAddress() != null &&
					this.getIpAddress().equals(other.getIpAddress())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
		.append( ipAddress )
		.append(gateway)
		.append(comment)
		.toHashCode();
	}


	public Set<IPTeaming> getIpTeaming() {
		return ipTeaming;
	}

	public void setIpTeaming(Set<IPTeaming> ipTeaming) {
		this.ipTeaming = ipTeaming;
	}

	@OneToMany(mappedBy="ipAddress",targetEntity=IPTeaming.class )
	@Cascade({CascadeType.ALL})
	private Set<IPTeaming> ipTeaming;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="tblipaddresses_id_seq")
	@SequenceGenerator(name="tblipaddresses_id_seq", sequenceName="tblipaddresses_id_seq", allocationSize=1)
	@Column(name="`id`")
	private long id;

	@Column(name="`ipaddress`")
	private String ipAddress; 

	@ManyToOne(fetch=FetchType.EAGER,targetEntity=NetMask.class)
	@JoinColumn(name="`maskid`")
	private NetMask mask;

	@Column(name="`gateway`")
	private String gateway;

	@Column(name="`dnsname`")
	private String dnsName;

	@Column(name="`networkid`")
	private Long networkId;

	@Column(name="`virtual`")
	private Boolean isVirtual;

	@Column(name="`comment`")
	private String comment;

	@ManyToOne(fetch=FetchType.EAGER,targetEntity=LkuData.class)
	@JoinColumn(name="`domainid`")
	private LkuData domain;

	@Transient
	private Boolean isDuplicatIpAllowed;

	@Transient
	private Boolean isTeamingAllowed;

	@Transient
	private Boolean isIpBeingGatewayAllowed;


}
