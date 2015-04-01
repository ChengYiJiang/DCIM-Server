/**
 * 
 */
package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.raritan.tdz.domain.DataCenterLocationDetails;

/**
 * @author prasanna
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class FloorJSON implements SitesJSON{
	
	public static String objectType = "FLOOR:"; 
	public static String externalKeyPrefix = "DCT Floor -- "; 

	Floor floor;

	List<Floor> floors;

	FloorJSON(){
		super();
	}
	
	public FloorJSON(DataCenterLocationDetails location){
		floor = new Floor();
		floor.setName(location.getCode());
		floor.setCapacity(null);
		floor.setExternalKey(externalKeyPrefix + location.getDataCenterLocationId());
	}
	
	@JsonProperty(value="floor")
	public Floor getFloor() {
		return floor;
	}

	@JsonSetter(value="floor")
	public void setFloor(Floor floor) {
		this.floor = floor;
	}

	@JsonProperty(value="floors")
	public List<Floor> getFloors() {
		return floors;
	}

	@JsonSetter(value="floors")
	public void setFloors(List<Floor> floors) {
		this.floors = floors;
	}
	
	@Override
	public String getId() {
		if (floor != null) 
			return floor.getId();
		
		return null;
	}
	
	@Override
	public String getExternalKey() {
		if (floor != null) 
			return floor.getExternalKey();
		
		return null;
	}
	
	public String getExternalComplexKey() {
		return objectType + getExternalKey();
	}

	public String getSiteId() {
		if (floors != null && !floors.isEmpty()) {
			return floors.get(0).getId();
		}
		return null;
	}
	
	public Boolean isDataCenterInSync(DataCenterLocationDetails location) {
		if (floor != null ) {
			return floor.isFloorInSync(location);
		}
		return false;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Floor {
		String id;
		String name;
		String externalKey;
		Double capacity;
		
		public Floor() {
			super();
		}

		public Boolean isFloorInSync(DataCenterLocationDetails location){
			Boolean result = false;
			
			String genId = null;
			
			if (getId() != null && location.getPiqId() != null){
				String[] ids = location.getPiqId().split(":");
				String id = "";
				
				for (int l = 0; l < ids.length - 2; l++)
					id += ids[l] + ":";
				genId = id + "Floor:" + getId();
			}
			
			if (
					//TODO:There is a bug in Windows client and therefore I cannot compare the name for now.
				//(getName() != null && location.getCode() != null && getName().equalsIgnoreCase(location.getCode()))
				//&& 
				(genId != null && location.getPiqId() != null && (genId).equals(location.getPiqId().toString()))
//				&& (getExternalKey() != null && getExternalKey().equals(externalKeyPrefix + location.getDataCenterLocationId()))
					)
			{
				result = true;
			}
			return result;
		}
		
		@JsonProperty(value="id")
		public String getId() {
			return id;
		}
		@JsonSetter(value="id")
		public void setId(String id) {
			this.id = id;
		}
		@JsonProperty(value="name")
		public String getName() {
			return name;
		}
		@JsonSetter(value="name")
		public void setName(String name) {
			this.name = name;
		}
		@JsonProperty(value="external_key")
		public String getExternalKey() {
			return externalKey;
		}
		@JsonSetter(value="external_key")
		public void setExternalKey(String externalKey) {
			this.externalKey = externalKey;
		}
		@JsonProperty(value="capacity")
		public Double getCapacity() {
			return capacity;
		}
		@JsonSetter(value="capacity")
		public void setCapacity(Double capacity) {
			this.capacity = capacity;
		}
		
	}
}
