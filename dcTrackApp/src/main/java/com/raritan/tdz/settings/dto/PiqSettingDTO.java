package com.raritan.tdz.settings.dto;

import java.io.Serializable;

/** CR56619 Populate PIQ Version.  */

public class PiqSettingDTO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1763798431535483512L;

	public PiqSettingDTO(){
		
	}
	
	private String ipAddress = "";

	private String settingLabel = "";
	
	private String userName = "";
	
	private String password = "";
	
	private String version = "";
	
	private String versionAppSid = "";
			
	private String integration = "";
	
	private String dataLastPushed = "";
	
	private String appSettingId = "";
	
	private String parentAppSettingId = "";
	
	private String pollingInterval = "";
	
	private String unsaved = "true";
	
	private String resultCode = "";
	
	public String getVersionAppSid() {
		return versionAppSid;
	}

	public void setVersionAppSid(String versionAppSid) {
		this.versionAppSid = versionAppSid;
	}

	
	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getUnsaved() {
		return unsaved;
	}

	public void setUnsaved(String unsaved) {
		this.unsaved = unsaved;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSettingLabel() {
		return settingLabel;
	}

	public void setSettingLabel(String settingLabel) {
		this.settingLabel = settingLabel;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIntegration() {
		return integration;
	}

	public void setIntegration(String integration) {
		this.integration = integration;
	}

	public String getDataLastPushed() {
		return dataLastPushed;
	}

	public void setDataLastPushed(String dataLastPushed) {
		this.dataLastPushed = dataLastPushed;
	}

	public String getAppSettingId() {
		return appSettingId;
	}

	public void setAppSettingId(String appSettingId) {
		this.appSettingId = appSettingId;
	}

	public String getParentAppSettingId() {
		return parentAppSettingId;
	}

	public void setParentAppSettingId(String parentAppSettingId) {
		this.parentAppSettingId = parentAppSettingId;
	}

	public String getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(String pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	
	
}
