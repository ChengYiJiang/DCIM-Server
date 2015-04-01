package com.raritan.tdz.searchplace.domain;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;

@Entity
@Table(name="`dct_place_search`")
public class PlaceSearch implements Serializable{

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_place_search_seq")
	@SequenceGenerator(name="dct_place_search_seq", sequenceName="dct_place_search_place_search_id_seq", allocationSize=1)
	@Column(name="`place_search_id`")
	private Long placeSearchId;

	
	public Long getPlaceSearchId() {
		return placeSearchId;
	}

	public void setPlaceSearchId(Long placeSearchId) {
		this.placeSearchId = placeSearchId;
	}

	@Column(name="`location_ids`")
	private String locationIds;
	public String getLocationIds() {
		return locationIds;
	}

	public void setLocationIds(String locationIds) {
		this.locationIds = locationIds;
	}
	
	@ManyToOne(fetch=FetchType.LAZY,targetEntity=Item.class)
	@JoinColumn(name="`item_id`")
	private Item item;
	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
	
	@ManyToOne(fetch=FetchType.LAZY,targetEntity=Item.class)
	@JoinColumn(name="`dest_cabinet_id`")
	private Item destCabinet;
	public Item getDestCabinet() {
		return destCabinet;
	}

	public void setDestCabinet(Item destCabinet) {
		this.destCabinet = destCabinet;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LkuData.class)
	@JoinColumn(name="`dest_group_lku_id`")
	private LkuData destGroup;
	public LkuData getDestGroup() {
		return destGroup;
	}

	public void setDestGroup(LkuData destGroup) {
		this.destGroup = destGroup;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LkuData.class)
	@JoinColumn(name="`dest_type_lku_id`")
	private LkuData destType;

	public LkuData getDestType() {
		return destType;
	}

	public void setDestType(LkuData destType) {
		this.destType = destType;
	}

	@ManyToOne(fetch=FetchType.LAZY,targetEntity=LkuData.class)
	@JoinColumn(name="`dest_function_lku_id`")
	private LkuData destFunction;
	public LkuData getDestFunction() {
		return destFunction;
	}

	public void setDestFunction(LkuData destFunction) {
		this.destFunction = destFunction;
	}

	@OneToMany(mappedBy="placeSearch",fetch=FetchType.LAZY, orphanRemoval = true, targetEntity=PlaceSearchPortPower.class)
	@Cascade({CascadeType.ALL})
	private Set<PlaceSearchPortPower> powerPorts;
	
	public Set<PlaceSearchPortPower> getPowerPorts() {
		return powerPorts;
	}

	public void setPowerPorts(Set<PlaceSearchPortPower> powerPorts) {
		this.powerPorts = powerPorts;
	}

	public void addPowerPort(PlaceSearchPortPower powerPort) {
		if (powerPorts == null) {
			powerPorts = new HashSet<PlaceSearchPortPower>();
		}
		powerPorts.add( powerPort );
	}
	
	public void removePowerPort(PlaceSearchPortPower powerPort) {
		if (powerPorts == null) {
			return;
		}
		powerPorts.remove(powerPort);
	}

	public void removeAllPowerPorts() {
		if (powerPorts == null) {
			return;
		}
		powerPorts.clear();
	}	


	@OneToMany(mappedBy="placeSearch",fetch=FetchType.LAZY, orphanRemoval = true, targetEntity=PlaceSearchPortData.class)
	@Cascade({CascadeType.ALL})
	private Set<PlaceSearchPortData> dataPorts;
	
	public Set<PlaceSearchPortData> getDataPorts() {
		return dataPorts;
	}

	public void setDataPorts(Set<PlaceSearchPortData> dataPorts) {
		this.dataPorts = dataPorts;
	}

	public void addDataPort(PlaceSearchPortData dataPort) {
		if (dataPorts == null) {
			dataPorts = new HashSet<PlaceSearchPortData>();
		}
		dataPorts.add( dataPort );
	}
	
	public void removeDataPort(PlaceSearchPortData dataPort) {
		if (dataPorts == null) {
			return;
		}
		dataPorts.remove(dataPort);
	}

	public void removeAllDataPorts() {
		if (dataPorts == null) {
			return;
		}
		dataPorts.clear();
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
	
	public PlaceSearch(){
		
	}
	public PlaceSearch(Long placeSearchId, String locationIds, Item item,
			Item destCabinet, LkuData destGroup, LkuData destType, LkuData destFunction,
			Set<PlaceSearchPortPower> powerPorts,
			Set<PlaceSearchPortData> dataPorts) {
		super();
		this.placeSearchId = placeSearchId;
		this.locationIds = locationIds;
		this.item = item;
		this.destCabinet = destCabinet;
		this.destGroup = destGroup;
		this.destType = destType;
		this.destFunction = destFunction;
		this.powerPorts = powerPorts;
		this.dataPorts = dataPorts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((placeSearchId == null) ? 0 : placeSearchId.hashCode());
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
		PlaceSearch other = (PlaceSearch) obj;
		if (placeSearchId == null) {
			if (other.placeSearchId != null)
				return false;
		} else if (!placeSearchId.equals(other.placeSearchId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlaceSearch [placeSearchId=" + placeSearchId + ", locationIds="
				+ locationIds + ", item=" + item + ", destCabinet="
				+ destCabinet + ", destGroup=" + destGroup + ", destType="
				+ destType + ", destFunction=" + destFunction + ", powerPorts="
				+ powerPorts + ", dataPorts=" + dataPorts
				+ ", sysCreationDate=" + sysCreationDate + ", sysCreatedBy="
				+ sysCreatedBy + "]";
	}

	
		
}

