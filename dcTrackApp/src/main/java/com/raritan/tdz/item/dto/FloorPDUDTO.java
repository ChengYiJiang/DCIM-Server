package com.raritan.tdz.item.dto;

public class FloorPDUDTO {
	String pduName; 
	String location; // site code
	String powerPanel; // power panel name
	String breaker; // breaker port name
	String rating; // power port ams_nameplate + volts_lks_id
	
	public String getPduName() {
		return pduName;
	}
	public void setPduName(String pduName) {
		this.pduName = pduName;
	}
	public String getPowerPanel() {
		return powerPanel;
	}
	public void setPowerPanel(String powerPanel) {
		this.powerPanel = powerPanel;
	}
	public String getBreaker() {
		return breaker;
	}
	public void setBreaker(String breaker) {
		this.breaker = breaker;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	
}
