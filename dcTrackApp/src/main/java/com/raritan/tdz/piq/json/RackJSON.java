/**
 * 
 */
package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.raritan.tdz.domain.Item;

/**
 * @author prasanna
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class RackJSON {

	private Rack rack;
	private List<Rack> racks;
	
	public static String externalKeyPrefix = "DCT Rack "; 
	public static String externalKeyPrefixPIQ = "Rack ";
	
	public RackJSON() {
		super();
	}
	
	public RackJSON(Item item, boolean update){
		rack = new Rack();
		rack.setName(item.getItemName());
		rack.setSpace_id(null);
		rack.setExternalKey(externalKeyPrefix + "-- " + item.getItemId());
		rack.setCapacity(null);
		Integer rackId = item.getPiqId();
		if (rackId != null)
			rack.setId(item.getPiqId().toString());
		String dc_piq_id = null;
		if (item.getDataCenterLocation() != null)
			dc_piq_id = item.getDataCenterLocation().getPiqId();
		if (!update && dc_piq_id != null)
		{
			rack.setParent(dc_piq_id);
		}
	}
	
	public void setExternalKey(Item item, boolean reset, String piqIdForReset){
		if (item != null){
			rack = new Rack();
			Integer rackId = item.getPiqId();
			if (rackId != null)
				rack.setId(item.getPiqId().toString());
			
			if (reset == true && piqIdForReset != null){
				rack.setId(piqIdForReset);
				String piqExternalKey = externalKeyPrefixPIQ + "-- " + piqIdForReset;
				rack.setExternalKey(piqExternalKey);
				item.setPiqExternalKey(piqExternalKey);
			} else if (reset == false){
				rack.setExternalKey(externalKeyPrefix + "-- " + item.getItemId());
			}
		}
	}
	
	@JsonProperty(value="rack")
	public Rack getRack() {
		return rack;
	}

	@JsonSetter(value="rack")
	public void setRack(Rack rack) {
		this.rack = rack;
	}

	/**
	 * @return the racks
	 */
	@JsonProperty(value="racks")
	public final List<Rack> getRacks() {
		return racks;
	}

	/**
	 * @param racks the racks to set
	 */
	@JsonSetter(value="racks")
	public final void setRacks(List<Rack> racks) {
		this.racks = racks;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Rack {
		private String id;
		private String name;
		private String space_id;
		private String externalKey;
		private Double capacity;
		private Parent parent;
		
		public Rack() {
			super();
		}

		public Boolean isRackInSync(Item item){
			Boolean result = false;
			
			Boolean externalKeyInSync = item.getItemName() != null && (externalKeyPrefix + "-- " + item.getItemId()).equals(getExternalKey());
			
			Boolean nameInSync = isNameInSync(item);
			
			if ((nameInSync)
				&& (getId() != null && item.getPiqId() != null && getId().equals(item.getPiqId().toString()))
//				&& (externalKeyInSync)
//				&& (getDCId(item) != null && getLocation(item) != null && getLocation(item).getId() != null 
//						&& getDCId(item).equals(getLocation(item).getId()))
					)
			{
				result = true;
			}
			return result;
		}
		

		
		private String getDCId(Item item){
			String piqId = null;
			String id = item.getDataCenterLocation().getPiqId();
			if (id != null && id.contains(":")){
				String ids[] = id.split(":");
				piqId = ids[ids.length - 1];
			}
			return piqId;
		}
		
		private Rack.Parent.Location getLocation(Item item){
			Rack.Parent.Location location = null;
			String id = item.getDataCenterLocation().getPiqId();
			String ids[] = id.split(":");
			String dcType = ids[ids.length - 2];
			
			if (parent != null){
				if (dcType.equals("DataCenter")){
					return parent.getDataCenter();
				}else if (dcType.equals("Floor")){
					return parent.getFloor();
				}else if (dcType.equals("Room")){
					return parent.getRoom();
				}
			}
			
			return location;
		}

		@JsonIgnoreProperties(ignoreUnknown=true)
		@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
		public static class Parent{
			
			private Location dataCenter;
			private Location floor;
			private Location room;
			
			public Parent() {
				super();
			}

			public Parent(String id){
				if (id != null && id.contains(":")){
					String ids[] = id.split(":");
					String dcType = ids[ids.length - 2];
					String dcId = ids[ids.length - 1];
					
					if (dcType.equals("DataCenter")){
						setDataCenter(new Location());
						getDataCenter().setId(dcId);
					}else if (dcType.equals("Floor")){
						setFloor(new Location());
						getFloor().setId(dcId);
					}else if (dcType.equals("Room")){
						setRoom(new Location());
						getRoom().setId(dcId);
					}
					
				}
			}
			
			@JsonIgnoreProperties(ignoreUnknown=true)
			public static class Location{
				private String id;
				
				public Location() {
					super();
				}

				@JsonProperty(value="id")
				public String getId() {
					return id;
				}

				@JsonSetter(value="id")
				public void setId(String id) {
					this.id = id;
				}
			}
			
		
			
			@JsonProperty(value="data_center")
			public Location getDataCenter() {
				return dataCenter;
			}

			@JsonSetter(value="data_center")
			public void setDataCenter(Location dataCenter) {
				this.dataCenter = dataCenter;
			}
			
			@JsonProperty(value="floor")
			public Location getFloor() {
				return floor;
			}

			@JsonSetter(value="floor")
			public void setFloor(Location floor) {
				this.floor = floor;
			}
			
			@JsonProperty(value="room")
			public Location getRoom() {
				return room;
			}

			@JsonSetter(value="room")
			public void setRoom(Location room) {
				this.room = room;
			}
			
			
			
//			DataCenterJSON.DataCenter dcJson;
//			
//			@JsonProperty(value="data_center")
//			public DataCenterJSON.DataCenter getDCJSON(){
//				return dcJson;
//			}
//			
//			@JsonSetter(value="data_center")
//			public void setDCJSON(DataCenterJSON.DataCenter dcJson){
//				this.dcJson = dcJson; 
//			}
//			
//			public Parent(String id){
//				DataCenterJSON.DataCenter dc = new DataCenterJSON.DataCenter();
//				dc.setId(id);
//				setDCJSON(dc);
//			}
			
		}
		
		@JsonProperty(value="parent")
		public Parent getParent() {
			return parent;
		}
		
		//@JsonIgnore
		@JsonSetter(value="parent")
		public void setParent(Parent parent) {
			this.parent = parent;
		}
		
		
		public void setParent(String id){
			setParent( new Parent(id) );
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
		
		@JsonProperty(value="space_id")
		public String getSpace_id() {
			return space_id;
		}
		
		@JsonSetter(value="space_id")
		public void setSpace_id(String space_id) {
			this.space_id = space_id;
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
		
		
		private Boolean isNameInSync(Item item){
			Boolean result = false;
			String piqName = getName() != null ? getName().toLowerCase() : null;
			String dcTrackName = item.getItemName() != null ? item.getItemName().toLowerCase() : null;
//	
//
//			//There may be situation where Windows client truncates names and puts (<number>) at the end
//			//We need to handle this situiation.
//			//The best way is to look if we have (<number>) in the dcTrack item's name
//			//Then we take the substring that does not contain (<number>) and compare this
//			//against the PIQName. If it matches, we are good and do not need to perform any updates
//			//If it does not match then we will update the PIQ name
//			//Please note that we have a big assumption here. The item names (<number>) is not something
//			//that user sets in, but Windows client sets during PIQ import (wizard)
//			//If we do not have a (<number>), it just does a "contains" comparision
//			if (getName() != null && item.getItemName() != null){
//				piqName = getName().toLowerCase();
//				dcTrackName = item.getItemName().toLowerCase();
//				
//				String regex1 = ".*\\(\\d+\\)";
//				if (dcTrackName.matches(regex1)){
//					String regex2 = "\\(";
//					String[] tokens = dcTrackName.split(regex2);
//					dcTrackName = tokens[0];
//				}
//			}
			
			if (piqName != null && dcTrackName != null && piqName.equalsIgnoreCase(dcTrackName)){
				result = true;
			}
			
			return result;
		}
		
	}
}
