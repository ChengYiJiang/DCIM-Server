/**
 * 
 */
package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

import com.raritan.tdz.piq.json.DataCenterJSON.DataCenter;
import com.raritan.tdz.piq.json.FloorJSON.Floor;
import com.raritan.tdz.piq.json.RoomJSON.Room;

/**
 * @author prasanna
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SiteParentJSON {
	
	DataCenter dc;
	Floor floor;
	Room room;

	SiteParentJSON(){
		super();
	}

	@JsonProperty(value="data_center")
	public DataCenter getDataCenter() {
		return dc;
	}

	@JsonSetter(value="data_center")
	public void setDataCenter(DataCenter dc) {
		this.dc = dc;
	}

	@JsonProperty(value="floor")
	public Floor getFloor() {
		return floor;
	}

	@JsonSetter(value="floor")
	public void setFloor(Floor floor) {
		this.floor = floor;
	}
	
	@JsonProperty(value="room")
	public Room getRoom() {
		return room;
	}

	@JsonSetter(value="room")
	public void setRoom(Room room) {
		this.room = room;
	}
	
	public String getId() {
		if (room != null && room.getId() != null) {
			return "Room:" + room.getId();
		}
		else if (floor != null && floor.getId() != null) {
			return "Floor:" + floor.getId();
		}
		else if (dc != null && dc.getId() != null) {
			return "DataCenter:" + dc.getId();
		}
		return "";
	}
}
