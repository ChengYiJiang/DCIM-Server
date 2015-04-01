package com.raritan.tdz.ip.domain;
import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.raritan.tdz.domain.DataPort;


@Entity
@Table(name = "`tblipteaming`")
public class IPTeaming implements Serializable {

	private static final long serialVersionUID = 227546574673367148L;

	public IPTeaming() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public DataPort getDataPort() {
		return dataPort;
	}

	public void setDataPort(DataPort dataPort) {
		this.dataPort = dataPort;
	}

	public IPAddressDetails getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(IPAddressDetails ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="tblipteaming_id_seq")
	@SequenceGenerator(name="tblipteaming_id_seq", sequenceName="tblipteaming_id_seq", allocationSize=1)
	@Column(name="id")
	Long id;

    @OneToOne(fetch=FetchType.LAZY,targetEntity=DataPort.class)
    @JoinColumn(name="`portid`")
    private DataPort dataPort;

	@ManyToOne(targetEntity = IPAddressDetails.class)
    @JoinColumn(name="`ipaddressid`")
    private IPAddressDetails ipAddress;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataPort == null) ? 0 : dataPort.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((ipAddress == null) ? 0 : ipAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IPTeaming other = (IPTeaming) obj;
		if (dataPort == null) {
			if (other.dataPort != null)
				return false;
		} else if (!dataPort.equals(other.dataPort))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (ipAddress == null) {
			if (other.ipAddress != null)
				return false;
		} else if (!ipAddress.equals(other.ipAddress))
			return false;
		return true;
	}
   

}
