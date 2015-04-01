/**
 * 
 */
package com.raritan.tdz.dctimport.dto;

import java.util.Map;

/**
 * @author prasanna
 *
 */
public class LocationImport extends DCTImportBase {
	
	@Header(value="dctracklocationcode")
	private String tiLocationCode;
	
	@Header(value="newdctracklocationcode")
	private String newLocationCode;
	
	@Header(value="outerroomdim-l")
	private Integer tiRoomLength;
	@Header(value="outerroomdim-w")
	private Integer tiRoomWidth;	
	
	@Header(value="datacenterheightraisedfloor")
	private Integer tiRaisedFloorHeight = -1;
	@Header(value="datacenterheightfinishedfloortofinishedceiling")
	private Integer tiFloorCeilingHeight = -1;
	@Header(value="datacenterheightfinishedceilingtoslabceiling")
	private Integer tiPlenumCeilingHeight = -1;
	
	@Header(value="verticalroworientation")
	private Boolean cmbOrientationNorthSouth;
	@Header(value="horizontalroworientation")
	private Boolean cmbOrientationEastWest;
	@Header(value="locationpicturepath")
	private String tiLocationPicturePath;
	
	@Header(value="datacenterarea")
	private Integer tiLocationArea;
	
	@Header(value="dctracklocationname")
	private String tiLocationName;
	private String tiLocationCADPath;
	
	@Header(value="country")
	private String cmbAddressCountry;
	@Header(value="floor")
	private String tiAddressFloor;
	@Header(value="street")
	private String tiAddressStreet;
	@Header(value="city")
	private String tiAddressCity;
	@Header(value="state")
	private String tiAddressState;
	@Header(value="postalcode")
	private String tiAddressZip;
	
	@Header(value="locationtype")
	private String cmbLocationType;
	
	@Header(value="units")
	private String units;

	@Header(value="drawingnorth")
	private String cmbDrawingNorth;
	@Header(value="gridlabel")
	private Boolean cmbXAxisNumbers;
	
	@Header(value="isdefaultlocation")
	private Boolean cbDefaultSite;
	
	@Header(value="enablevirtualpowerchain")
	private Boolean enableVPC;

	@Header(value="poweriqappliancename,ipaddressorhostname")
	private String piqMappingName;

	@Header(value="poweriqobjecttype")
	private String piqObjectType;
	@Header(value="poweriqobjectexternalkey")
	private String tiPiqExternalKey;
	@Header(value="poweriqobjectname")
	private String piqObjectName;

	public LocationImport(){
		super();
	}

	public LocationImport(Map<String, String> headerMap) {
		super(headerMap);
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}
	
	public Integer getTiRoomLength() {
		return tiRoomLength;
	}
	
	public void setTiRoomLength(Integer tiRoomLength) {
		this.tiRoomLength = tiRoomLength;
	}
	
	public Boolean getCmbOrientationNorthSouth() {
		return cmbOrientationNorthSouth;
	}
	
	public void setCmbOrientationNorthSouth(Boolean cmbOrientationNorthSouth) {
		this.cmbOrientationNorthSouth = cmbOrientationNorthSouth;
	}
	
	public Integer getTiRaisedFloorHeight() {
		return tiRaisedFloorHeight;
	}
	
	public void setTiRaisedFloorHeight(Integer tiRaisedFloorHeight) {
		this.tiRaisedFloorHeight = tiRaisedFloorHeight;
	}
	
	public Boolean getCmbOrientationEastWest() {
		return cmbOrientationEastWest;
	}
	
	public void setCmbOrientationEastWest(Boolean cmbOrientationEastWest) {
		this.cmbOrientationEastWest = cmbOrientationEastWest;
	}
	
	public String getTiLocationPicturePath() {
		return tiLocationPicturePath;
	}
	
	public void setTiLocationPicturePath(String tiLocationPicturePath) {
		this.tiLocationPicturePath = tiLocationPicturePath;
	}
	
	public String getTiAddressCity() {
		return tiAddressCity;
	}
	
	public void setTiAddressCity(String tiAddressCity) {
		this.tiAddressCity = tiAddressCity;
	}
	
	public String getTiAddressStreet() {
		return tiAddressStreet;
	}
	
	public void setTiAddressStreet(String tiAddressStreet) {
		this.tiAddressStreet = tiAddressStreet;
	}
	
	public Integer getTiLocationArea() {
		return tiLocationArea;
	}
	
	public void setTiLocationArea(Integer tiLocationArea) {
		this.tiLocationArea = tiLocationArea;
	}
	
	public Integer getTiPlenumCeilingHeight() {
		return tiPlenumCeilingHeight;
	}
	
	public void setTiPlenumCeilingHeight(Integer tiPlenumCeilingHeight) {
		this.tiPlenumCeilingHeight = tiPlenumCeilingHeight;
	}
	
	public Integer getTiRoomWidth() {
		return tiRoomWidth;
	}
	
	public void setTiRoomWidth(Integer tiRoomWidth) {
		this.tiRoomWidth = tiRoomWidth;
	}
	
	public String getTiAddressFloor() {
		return tiAddressFloor;
	}
	
	public void setTiAddressFloor(String tiAddressFloor) {
		this.tiAddressFloor = tiAddressFloor;
	}
	
	public String getTiAddressZip() {
		return tiAddressZip;
	}
	
	public void setTiAddressZip(String tiAddressZip) {
		this.tiAddressZip = tiAddressZip;
	}
	
	public String getTiAddressState() {
		return tiAddressState;
	}
	
	public void setTiAddressState(String tiAddressState) {
		this.tiAddressState = tiAddressState;
	}
	
	public String getTiLocationName() {
		return tiLocationName;
	}
	
	public void setTiLocationName(String tiLocationName) {
		this.tiLocationName = tiLocationName;
	}
	
	public String getTiLocationCADPath() {
		return tiLocationCADPath;
	}
	
	public void setTiLocationCADPath(String tiLocationCADPath) {
		this.tiLocationCADPath = tiLocationCADPath;
	}
	
	public String getCmbAddressCountry() {
		return cmbAddressCountry;
	}
	
	public void setCmbAddressCountry(String cmbAddressCountry) {
		this.cmbAddressCountry = cmbAddressCountry;
	}
	
	public Integer getTiFloorCeilingHeight() {
		return tiFloorCeilingHeight;
	}
	
	public void setTiFloorCeilingHeight(Integer tiFloorCeilingHeight) {
		this.tiFloorCeilingHeight = tiFloorCeilingHeight;
	}
	
	public String getCmbLocationType() {
		return cmbLocationType;
	}
	
	public void setCmbLocationType(String cmbLocationType) {
		this.cmbLocationType = cmbLocationType;
	}
	
	public String getCmbDrawingNorth() {
		return cmbDrawingNorth;
	}
	
	public void setCmbDrawingNorth(String cmbDrawingNorth) {
		this.cmbDrawingNorth = cmbDrawingNorth;
	}
	
	public Boolean getCbDefaultSite() {
		return cbDefaultSite;
	}
	
	public void setCbDefaultSite(Boolean cbDefaultSite) {
		this.cbDefaultSite = cbDefaultSite;
	}
	
	public String getTiLocationCode() {
		return tiLocationCode;
	}
	
	public void setTiLocationCode(String tiLocationCode) {
		this.tiLocationCode = tiLocationCode;
	}
	
	public Boolean getCmbXAxisNumbers() {
		return cmbXAxisNumbers;
	}
	
	public void setCmbXAxisNumbers(Boolean cmbXAxisNumbers) {
		this.cmbXAxisNumbers = cmbXAxisNumbers;
	}
	
	public Boolean getEnableVPC() {
		return enableVPC;
	}

	public void setEnableVPC(Boolean enableVPC) {
		this.enableVPC = enableVPC;
	}

	
	public String getPiqMappingName() {
		return piqMappingName;
	}

	public void setPiqMappingName(String piqMappingName) {
		this.piqMappingName = piqMappingName;
	}

	public String getPiqObjectType() {
		return piqObjectType;
	}

	public void setPiqObjectType(String piqObjectType) {
		this.piqObjectType = piqObjectType;
	}

	public String getPiqObjectName() {
		return piqObjectName;
	}

	public void setPiqObjectName(String piqObjectName) {
		this.piqObjectName = piqObjectName;
	}
	
	/**
	 * @return the tiPiqExternalKey
	 */
	public String getTiPiqExternalKey() {
		return tiPiqExternalKey;
	}

	/**
	 * @param tiPiqExternalKey the tiPiqExternalKey to set
	 */
	public void setTiPiqExternalKey(String tiPiqExternalKey) {
		this.tiPiqExternalKey = tiPiqExternalKey;
	}

	
	public String getNewLocationCode() {
		return newLocationCode;
	}

	public void setNewLocationCode(String newLocationCode) {
		this.newLocationCode = newLocationCode;
	}

	@Override
	public String toString() {
		return "LocationImport [enableVPC=" + enableVPC + ", tiRoomLength="
				+ tiRoomLength + ", cmbOrientationNorthSouth="
				+ cmbOrientationNorthSouth + ", tiRaisedFloorHeight="
				+ tiRaisedFloorHeight + ", cmbOrientationEastWest="
				+ cmbOrientationEastWest + ", tiLocationPicturePath="
				+ tiLocationPicturePath + ", tiAddressCity=" + tiAddressCity
				+ ", tiAddressStreet=" + tiAddressStreet + ", tiLocationArea="
				+ tiLocationArea + ", tiPlenumCeilingHeight="
				+ tiPlenumCeilingHeight + ", tiRoomWidth=" + tiRoomWidth
				+ ", tiAddressFloor=" + tiAddressFloor + ", tiAddressZip="
				+ tiAddressZip + ", tiAddressState=" + tiAddressState
				+ ", tiLocationName=" + tiLocationName + ", tiLocationCADPath="
				+ tiLocationCADPath + ", cmbAddressCountry="
				+ cmbAddressCountry + ", tiFloorCeilingHeight="
				+ tiFloorCeilingHeight + ", cmbLocationType=" + cmbLocationType
				+ ", cmbDrawingNorth=" + cmbDrawingNorth + ", cbDefaultSite="
				+ cbDefaultSite + ", tiLocationCode=" + tiLocationCode
				+ ", cmbXAxisNumbers=" + cmbXAxisNumbers
				+ ", units=" + units
				+ ", tiPiqExternalKey=" + tiPiqExternalKey
				+ ", newLocationCode=" + newLocationCode + "]";
	}
	

}
