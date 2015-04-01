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
public class RoomJSON implements SitesJSON{

	public static String objectType = "ROOM:";
	public static String externalKeyPrefix = "DCT Room -- ";

	Room room;

	List<Room> rooms;

	RoomJSON(){
		super();
	}
	
	public RoomJSON(DataCenterLocationDetails location){
		room = new Room();
		room.setName(location.getCode());
		room.setCapacity(null);
		room.setExternalKey(externalKeyPrefix + location.getDataCenterLocationId());
	}
	
	@JsonProperty(value="room")
	public Room getRoom() {
		return room;
	}

	@JsonSetter(value="room")
	public void setRoom(Room room) {
		this.room = room;
	}

	@JsonProperty(value="rooms")
	public List<Room> getRooms() {
		return rooms;
	}

	@JsonSetter(value="rooms")
	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}
	
	@Override
	public String getId() {
		if (room != null) 
			return room.getId();
		
		return null;
	}
	
	@Override
	public String getExternalKey() {
		if (room != null) 
			return room.getExternalKey();
		
		return null;
	}
	
	public String getExternalComplexKey() {
		return objectType + getExternalKey();
	}

	public String getSiteId() {
		if (rooms != null && !rooms.isEmpty()) {
			return rooms.get(0).getId();
		}
		return null;
	}
	
	public Boolean isDataCenterInSync(DataCenterLocationDetails location) {
		if (room != null ) {
			return room.isRoomInSync(location);
		}
		
		return false;
	}
	
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Room {
		String id;
		String name;
		String externalKey;
		Double capacity;
		
		public Room() {
			super();
		}

		public Boolean isRoomInSync(DataCenterLocationDetails location){
			Boolean result = false;
			
			String genId = null;
			
			if (getId() != null && location.getPiqId() != null){
				String[] ids = location.getPiqId().split(":");
				String id = "";
				
				for (int l = 0; l < ids.length - 2; l++)
					id += ids[l] + ":";
				genId = id + "Room:" + getId();
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
