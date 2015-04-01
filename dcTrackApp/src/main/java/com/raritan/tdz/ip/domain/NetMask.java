package com.raritan.tdz.ip.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Domain object representing Netmask.
 */
@Entity
@Table(name = "`tlksnetmasks`")
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class NetMask implements Serializable {

	private static final long serialVersionUID = 1L;

	public NetMask() {
	}
	
	public NetMask(NetMask netMask) {
		this.mask = netMask.mask;
		this.cidr = netMask.cidr;
		this.firstNet = netMask.firstNet;
		this.hosts = netMask.hosts;
		this.networks = netMask.networks;
		this.nextNet = netMask.nextNet;
		this.total = netMask.total;
		this.id = netMask.id;
		
	}
	
	@Override
    public Object clone() {
            NetMask copy = new NetMask();
            if( this.getMask() != null ){
            	copy.mask = new String(this.getMask());
            }
            if( this.getCidr() != null ){
            	copy.cidr = new Long(this.getCidr());
            }
            	copy.firstNet = new String(this.getFirstNet());
            	if( this.getFirstNet() != null ){
            }
            if(this.getHosts() != null ){
            	copy.hosts = new Long(this.getHosts());
            }
            if( this.getNetworks() != null ){
            	copy.networks = new Long(this.getNetworks());
            }
            if( this.getNextNet() != null ){
            	copy.nextNet = new String(this.getNextNet());
            }
            if( this.getTotal() != null ){
            	copy.total = new Long(this.getTotal());
            }
            copy.id = this.getId() ;
            return copy;
    }

	@JsonProperty("id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public Long getHosts() {
		return hosts;
	}

	public void setHosts(Long hosts) {
		this.hosts = hosts;
	}

	public Long getNetworks() {
		return networks;
	}

	public void setNetworks(Long networks) {
		this.networks = networks;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public String getFirstNet() {
		return firstNet;
	}

	public void setFirstNet(String firstNet) {
		this.firstNet = firstNet;
	}

	public String getNextNet() {
		return nextNet;
	}

	public void setNextNet(String nextNet) {
		this.nextNet = nextNet;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		
		if (obj instanceof NetMask) {
			NetMask other = (NetMask)obj;
			if (this.getMask() != null &&
					other.getMask() != null &&
					this.getMask().equals(other.getMask())) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append( mask )
			.toHashCode();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="tlksnetmasks_seq")
	@SequenceGenerator(name="tlksnetmasks_seq", sequenceName="tlksnetmasks_id_seq", allocationSize=1)
	@Column(name="id")
	private long id;
	
	@Column(name="mask")
	private String mask; 

	@Column(name="cidr")
	private Long cidr;
	
	@Column(name="hosts")
	private Long hosts;
	
	@Column(name="networks")
	private Long networks;
	
	@Column(name="total")
	private Long total;
	
	@Column(name="firstnet")
	private String firstNet;
	
	@Column(name="nextnet")
	private String nextNet;
	
	
}
