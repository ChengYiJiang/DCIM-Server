package com.raritan.tdz.searchplace.domain;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;

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

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.DataPort;

@Entity
@Table(name="`dct_place_search_ports_data`")
public class PlaceSearchPortData implements Serializable{

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_place_search_ports_data_seq")
	@SequenceGenerator(name="dct_place_search_ports_data_seq", sequenceName="dct_place_search_ports_data_place_search_port_data_id_seq", allocationSize=1)
	@Column(name="`place_search_port_data_id`")
	private Long placeSearchPortDataId;
	public Long getPlaceSearchPortDataId() {
		return placeSearchPortDataId;
	}

	public void setPlaceSearchPortDataId(Long placeSearchPortDataId) {
		this.placeSearchPortDataId = placeSearchPortDataId;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=PlaceSearch.class)
	@JoinColumn(name="`place_search_id`")
	private PlaceSearch placeSearch;
	public PlaceSearch getPlaceSearch() {
		return placeSearch;
	}

	public void setPlaceSearch(PlaceSearch placeSearch) {
		this.placeSearch = placeSearch;
	}
	
	@ManyToOne(fetch=FetchType.LAZY,targetEntity=DataPort.class)
	@JoinColumn(name="`port_data_id`")
	private DataPort dataPort;	
	public DataPort getDataPort() {
		return dataPort;
	}

	public void setDataPort(DataPort dataPort) {
		this.dataPort = dataPort;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LksData.class)
	@JoinColumn(name="`class_lks_id`")
	private LksData classLookup;
	public LksData getClassLookup() {
		return classLookup;
	}

	public void setClassLookup(LksData classLookup) {
		this.classLookup = classLookup;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=ConnectorLkuData.class)
	@JoinColumn(name="`connector_lku_id`")
	private ConnectorLkuData connectorLookup;
	public ConnectorLkuData getConnectorLookup() {
		return connectorLookup;
	}

	public void setConnectorLookup(ConnectorLkuData connectorLookup) {
		this.connectorLookup = connectorLookup;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LkuData.class)
	@JoinColumn(name="`color_lku_id`")
	private LkuData colorLookup;
	public LkuData getColorLookup() {
		return colorLookup;
	}

	public void setColorLookup(LkuData colorLookup) {
		this.colorLookup = colorLookup;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LksData.class)
	@JoinColumn(name="`media_lks_id`")
	private LksData mediaLookup;
	public LksData getMediaLookup() {
		return mediaLookup;
	}

	public void setMediaLookup(LksData mediaLookup) {
		this.mediaLookup = mediaLookup;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LkuData.class)
	@JoinColumn(name="`vlan_lku_id`")
	private LkuData vlanLookup;
	public LkuData getVlanLookup() {
		return vlanLookup;
	}

	public void setVlanLookup(LkuData vlanLookup) {
		this.vlanLookup = vlanLookup;
	}

	@Column(name="`sys_creation_date`")
	private Date sysCreationDate;	
	public Date getSysCreationDate() {
		return sysCreationDate;
	}

	public void setSysCreationDate(Date sysCreationDate) {
		this.sysCreationDate = sysCreationDate;
		if (sysCreationDate == null) {
			// If creation date not specified, set default creation date to now since this cannot be null
			this.sysCreationDate = new Date(Calendar.getInstance().getTimeInMillis());
		}
	}
	

	@Column(name="`sys_created_by`")
	private String sysCreatedBy;
	public String getSysCreatedBy() {
		return sysCreatedBy;
	}

	public void setSysCreatedBy(String sysCreatedBy) {
		this.sysCreatedBy = sysCreatedBy;
	}
	
	
	public PlaceSearchPortData(){
		
	}
	
	public PlaceSearchPortData(Long placeSearchPortDataId,
			PlaceSearch placeSearch, DataPort dataPort, LksData classLookup,
			ConnectorLkuData connectorLookup, LkuData colorLookup,
			LksData mediaLookup, LkuData vlanLookup) {
		super();
		this.placeSearchPortDataId = placeSearchPortDataId;
		this.placeSearch = placeSearch;
		this.dataPort = dataPort;
		this.classLookup = classLookup;
		this.connectorLookup = connectorLookup;
		this.colorLookup = colorLookup;
		this.mediaLookup = mediaLookup;
		this.vlanLookup = vlanLookup;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((placeSearchPortDataId == null) ? 0 : placeSearchPortDataId
						.hashCode());
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
		PlaceSearchPortData other = (PlaceSearchPortData) obj;
		if (placeSearchPortDataId == null) {
			if (other.placeSearchPortDataId != null)
				return false;
		} else if (!placeSearchPortDataId.equals(other.placeSearchPortDataId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlaceSearchPortData [placeSearchPortDataId="
				+ placeSearchPortDataId + ", placeSearch=" + placeSearch
				+ ", dataPort=" + dataPort + ", classLookup=" + classLookup
				+ ", connectorLookup=" + connectorLookup + ", colorLookup="
				+ colorLookup + ", mediaLookup=" + mediaLookup
				+ ", vlanLookup=" + vlanLookup + ", sysCreationDate="
				+ sysCreationDate + ", sysCreatedBy=" + sysCreatedBy + "]";
	}

	
}
