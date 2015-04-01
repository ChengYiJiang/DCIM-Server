package com.raritan.tdz.dctimport.dto;

import java.util.List;

/**
 * import file for structured cabling connections.
 * @author KC
 *
 */
public class StructuredCablingConnImport extends DCTImportBase {
	private Float circuitId;
	private Long statusLksValueCode;
	
	@Header(value="locationstartingitem")
	private String locationStartingItem;
	
	@Header(value="startingitemname")
	private String startingItemName;
	
	@Header(value="startingportname")
	private String startingPortName;
	
	@Header(value="startingportconnector")
	private String startingPortConnector;
	
	@Header(value="cablemedia")
	private String cableMedia;
	
	@Header(value="length(ft/m)")
	private String cableLength;
	
	@Header(value="lengthunits")
	private String lengthUnits;
	
	@Header(value="color")
	private String cableColor;
	
	@Header(value="group")
	private String group;
			
	@Header(value="locationendingitem")
	private String locationEndingItem;
			
	@Header(value="endingitemname")
	private String endingItemName;
			
	@Header(value="endingportname")
	private String endingPortName;
	
	@Header(value="endingportconnector")
	private String endingPortConnector;

	public StructuredCablingConnImport() {
		super();
	}
	
	public String getLocationStartingItem() {
		return locationStartingItem;
	}

	public void setLocationStartingItem(String locationStartingItem) {
		this.locationStartingItem = locationStartingItem;
	}

	public String getStartingPortConnector() {
		return startingPortConnector;
	}

	public void setStartingPortConnector(String startingPortConnector) {
		this.startingPortConnector = startingPortConnector;
	}

	public String getCableMedia() {
		return cableMedia;
	}

	public void setCableMedia(String cableMedia) {
		this.cableMedia = cableMedia;
	}

	public String getCableLength() {
		return cableLength;
	}

	public void setCableLength(String cableLength) {
		this.cableLength = cableLength;
	}

	public String getLengthUnits() {
		return lengthUnits;
	}

	public void setLengthUnits(String lengthUnits) {
		this.lengthUnits = lengthUnits;
	}

	public String getCableColor() {
		return cableColor;
	}

	public void setCableColor(String cableColor) {
		this.cableColor = cableColor;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getLocationEndingItem() {
		return locationEndingItem;
	}

	public void setLocationEndingItem(String locationEndingItem) {
		this.locationEndingItem = locationEndingItem;
	}

	public String getEndingPortConnector() {
		return endingPortConnector;
	}

	public void setEndingPortConnector(String endingPortConnector) {
		this.endingPortConnector = endingPortConnector;
	}

	public String getStartingItemName() {
		return startingItemName;
	}

	public void setStartingItemName(String startingItemName) {
		this.startingItemName = startingItemName;
	}

	public String getStartingPortName() {
		return startingPortName;
	}

	public void setStartingPortName(String startingPortName) {
		this.startingPortName = startingPortName;
	}

	
	public String getEndingItemName() {
		return endingItemName;
	}

	public void setEndingItemName(String endingItemName) {
		this.endingItemName = endingItemName;
	}

	public String getEndingPortName() {
		return endingPortName;
	}

	public void setEndingPortName(String endingPortName) {
		this.endingPortName = endingPortName;
	}

	public Float getCircuitId() {
		return circuitId;
	}

	public void setCircuitId(Float circuitId) {
		this.circuitId = circuitId;
	}

	public Long getStatusLksValueCode() {
		return statusLksValueCode;
	}

	public void setStatusLksValueCode(Long statusLksValueCode) {
		this.statusLksValueCode = statusLksValueCode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder("");
		sb.append("StructuredCablingConnImport [circuitId=").append(circuitId);
		sb.append(", statusLksValueCode=").append(statusLksValueCode);
		sb.append(", locationStartingItem=").append(locationStartingItem);
		sb.append(", startingItemName=").append(startingItemName);
		sb.append(", startingPortName=").append(startingPortName);
		sb.append(", startingPortConnector=").append(startingPortConnector);
		sb.append(", cableMedia=").append(cableMedia);
		sb.append(", length=").append(cableLength);
		sb.append(", lengthUnits=").append(lengthUnits);
		sb.append(", color=").append(cableColor);
		sb.append(", group=").append(group);
		sb.append(", locationEndingItem=").append(locationEndingItem);
		sb.append(", endingItemName=").append(endingItemName);
		sb.append(", endingPortName=").append(endingPortName);
		sb.append(", endingPortConnector=").append(endingPortConnector);
		sb.append("]");
		return  sb.toString(); 				 								
	}

}
