package com.raritan.tdz.settings.dto;

import java.io.Serializable;

import com.raritan.tdz.domain.ApplicationSetting;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.location.dao.LocationDAO;

/**
 * An application setting. This DTO is used for reading AND writing application settings to/from the database.
 * 
 * @author Andrew Cohen
 */
public class ApplicationSettingDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long appSettingId;
	private String settingLksValue;
	private String locationCode;
	private long settingLksId;
	private long settingLkpValueCode;
	private long locationId;
	private long parentAppSettingId = -1;
	private String appValue;
	private String parentAppValue;
	private String label;
	
	//This will be used to group together the settings for a specific row displayed in the UI
	//Currently used for PowerIQSettings. Note that this will not go into the database. This is 
	//a transient string. This string must be unique. One way to make it unique is to 
	//use the session_key and a row number combination. For example 134jkksk344cc2103_1, etc.
	private String parentGroupingId;
	
	private ApplicationSetting appSetting;
	

	public ApplicationSettingDTO(ApplicationSetting appSetting, LocationDAO locationDAO) {
		this.appSetting = appSetting;
		
		if (appSetting != null){
			appSettingId = appSetting.getId();
			
			LksData lksData = appSetting.getLksData();
			settingLksId = lksData != null ? lksData.getLksId() : -1;
			settingLksValue = lksData != null ? lksData.getLkpValue() : "";
			settingLkpValueCode = lksData != null ? lksData.getLkpValueCode() : -1;
			
			DataCenterLocationDetails location = appSetting.getLocationId() != null ? locationDAO.getLocation(appSetting.getLocationId()):null;
			locationId = location != null ? location.getDataCenterLocationId() : -1;
			locationCode = location != null ? location.getCode() : "";
			
			appValue = appSetting.getValue() != null ? appSetting.getValue() : "";
			
			ApplicationSetting parentApplicationSetting = appSetting.getParentAppSettings();
			parentAppSettingId = parentApplicationSetting != null ? parentApplicationSetting.getId(): -1;
			parentAppValue = parentApplicationSetting != null ? parentApplicationSetting.getValue(): "";
		}
	}
	
	public ApplicationSettingDTO() {
		appSettingId = -1;
		settingLksId = -1;
		settingLkpValueCode = -1;
		locationId = -1;
		settingLksValue = "";
		appValue = "";
		locationCode = "";
		parentAppValue = "";
		parentAppSettingId = -1;
	}

	public long getAppSettingId() {
		return appSettingId;
	}

	public void setAppSettingId(long appSettingId) {
		this.appSettingId = appSettingId;
	}

	public String getSettingLksValue() {
		return settingLksValue;
	}

	public void setSettingLksValue(String settingLksValue) {
		this.settingLksValue = settingLksValue;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getAppValue() {
		return appValue;
	}

	public void setAppValue(String appValue) {
		this.appValue = appValue;
	}
	
	
	public long getSettingLkpValueCode() {
		return settingLkpValueCode;
	}

	public void setSettingLkpValueCode(long settingLkpValueCode) {
		this.settingLkpValueCode = settingLkpValueCode;
	}

	
	//
	// The following getter methods are not exposed via BlazeDS since they do not have corresponding setters
	//

	public ApplicationSetting getAppSetting() {
		return appSetting;
	}
	
	public long getSettingLksId() {
		return settingLksId;
	}
	
	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}
	
	public long getLocationId() {
		return locationId;
	}
	
	
	
	public long getParentAppSettingId() {
		return parentAppSettingId;
	}

	public void setParentAppSettingId(long parentAppSettingId) {
		this.parentAppSettingId = parentAppSettingId;
	}
	
	

	public String getParentAppValue() {
		return parentAppValue;
	}

	public void setParentAppValue(String ParentAppValue) {
		this.parentAppValue = ParentAppValue;
	}

	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getParentGroupingId() {
		return parentGroupingId;
	}

	public void setParentGroupingId(String parentGroupingId) {
		this.parentGroupingId = parentGroupingId;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("{ id=");
		b.append( getAppSettingId() );
		b.append(", name=");
		b.append( getSettingLksValue() );
		b.append(", settingLkpValueCode=");
		b.append(getSettingLkpValueCode());
		b.append(", value=");
		b.append( getAppValue() );
		b.append(", location=");
		b.append( getLocationCode() );
		b.append(", parentSettingId =");
		b.append(parentAppSettingId);
		b.append(", ParentAppValue =");
		b.append(parentAppValue);
		b.append(", label =");
		b.append(label);
		b.append(" }");
		return b.toString();
	}
}
