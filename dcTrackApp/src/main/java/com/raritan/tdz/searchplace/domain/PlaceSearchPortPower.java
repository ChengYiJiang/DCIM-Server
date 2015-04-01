package com.raritan.tdz.searchplace.domain;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
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
import com.raritan.tdz.domain.PowerPort;

@Entity
@Table(name="`dct_place_search_ports_power`")
public class PlaceSearchPortPower implements Serializable{

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_place_search_ports_power_seq")
	@SequenceGenerator(name="dct_place_search_ports_power_seq", sequenceName="dct_place_search_ports_power_place_search_port_power_id_seq", allocationSize=1)
	@Column(name="`place_search_port_power_id`")
	private Long placeSearchPortPowerId;
	public Long getPlaceSearchPortPowerId() {
		return placeSearchPortPowerId;
	}

	public void setPlaceSearchPortPowerId(Long placeSearchPortPowerId) {
		this.placeSearchPortPowerId = placeSearchPortPowerId;
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

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=PowerPort.class)
	@JoinColumn(name="`port_power_id`")
	private PowerPort powerPort;	
	public PowerPort getPowerPort() {
		return powerPort;
	}

	public void setPowerPort(PowerPort powerPort) {
		this.powerPort = powerPort;
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
	@JoinColumn(name="`phase_lks_id`")
	private LksData phaseLookup;
	public LksData getPhaseLookup() {
		return phaseLookup;
	}

	public void setPhaseLookup(LksData phaseLookup) {
		this.phaseLookup = phaseLookup;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LksData.class)
	@JoinColumn(name="`volts_lks_id`")
	private LksData voltsLookup;
	public LksData getVoltsLookup() {
		return voltsLookup;
	}

	public void setVoltsLookup(LksData voltsLookup) {
		this.voltsLookup = voltsLookup;
	}

	@Column(name="`watts_nameplate`")
	private int wattsNameplate = 0;
	public int getWattsNameplate() {
		return wattsNameplate;
	}

	public void setWattsNameplate(int wattsNameplate) {
		this.wattsNameplate = wattsNameplate;
	}

	@Column(name="`watts_budget`")
	private int wattsBudget = 0;
	public int getWattsBudget() {
		return wattsBudget;
	}

	public void setWattsBudget(int wattsBudget) {
		this.wattsBudget = wattsBudget;
	}
	
	@Column(name="`redundancy`")
	private String redundancy;
	public String getRedundancy() {
		return redundancy;
	}

	public void setRedundancy(String redundancy) {
		this.redundancy = redundancy;
	}
	
	
	@Column(name="`quantity`")
	private String quantity;
	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
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
	
	
	public PlaceSearchPortPower(){
		
	}
	
	public PlaceSearchPortPower(Long placeSearchPortPowerId,
			PlaceSearch placeSearch, PowerPort powerPort, LksData classLookup,
			ConnectorLkuData connectorLookup, LkuData colorLookup,
			LksData phaseLookup, LksData voltsLookup, int wattsNameplate,
			int wattsBudget, String redundancy, String quantity
			) {
		super();
		this.placeSearchPortPowerId = placeSearchPortPowerId;
		this.placeSearch = placeSearch;
		this.powerPort = powerPort;
		this.classLookup = classLookup;
		this.connectorLookup = connectorLookup;
		this.colorLookup = colorLookup;
		this.phaseLookup = phaseLookup;
		this.voltsLookup = voltsLookup;
		this.wattsNameplate = wattsNameplate;
		this.wattsBudget = wattsBudget;
		this.redundancy = redundancy;
		this.quantity = quantity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((placeSearchPortPowerId == null) ? 0
						: placeSearchPortPowerId.hashCode());
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
		PlaceSearchPortPower other = (PlaceSearchPortPower) obj;
		if (placeSearchPortPowerId == null) {
			if (other.placeSearchPortPowerId != null)
				return false;
		} else if (!placeSearchPortPowerId.equals(other.placeSearchPortPowerId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlaceSearchPortPower [placeSearchPortPowerId="
				+ placeSearchPortPowerId + ", placeSearch=" + placeSearch
				+ ", powerPort=" + powerPort + ", classLookup=" + classLookup
				+ ", connectorLookup=" + connectorLookup + ", colorLookup="
				+ colorLookup + ", phaseLookup=" + phaseLookup
				+ ", voltsLookup=" + voltsLookup + ", wattsNameplate="
				+ wattsNameplate + ", wattsBudget=" + wattsBudget
				+ ", redundancy=" + redundancy + ", quantity=" + quantity
				+ ", sysCreationDate=" + sysCreationDate + ", sysCreatedBy="
				+ sysCreatedBy + "]";
	}

	
}


