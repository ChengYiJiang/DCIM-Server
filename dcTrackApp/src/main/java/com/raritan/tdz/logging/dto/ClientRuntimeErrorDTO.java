package com.raritan.tdz.logging.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * An object capturing information about a runtime error that occurred in the Flex Client.
 * @author Andrew Cohen
 */
public class ClientRuntimeErrorDTO implements Serializable {

	private static final long serialVersionUID = -3284464862919664898L;

	/** The stack trace of the runtime error */
	private String stackTrace;
	
	/** The time the error occured on the client */
	private Date time;
	
	/** The version of dcTrack */
	private String dcTrackVersion;
	
	/** The version of the flash player */
	private String flashPlayerVersion;
	
	/** Is Flash player the debug version or not */
	private boolean isDebuggerVersion = false;
	
	/** The browser name */
	private String browserName;
	
	/** The browser version */
	private String browserVersion;
	
	/** The browser agent */
	private String browserAgent;
	
	/** The client OS name and version */
	private String clientOS;
	
	/** The dcTrack username */
	private String user;
	
	public ClientRuntimeErrorDTO() {
	}
	
	// TODO: Add spring bean validation
	
	public String getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getDcTrackVersion() {
		return dcTrackVersion;
	}
	public void setDcTrackVersion(String dcTrackVersion) {
		this.dcTrackVersion = dcTrackVersion;
	}
	public String getFlashPlayerVersion() {
		return flashPlayerVersion;
	}
	public void setFlashPlayerVersion(String flashPlayerVersion) {
		this.flashPlayerVersion = flashPlayerVersion;
	}
	public String getBrowserName() {
		return browserName;
	}
	public void setBrowser(String browserName) {
		this.browserName = browserName;
	}
	public String getClientOS() {
		return clientOS;
	}
	public void setClientOS(String clientOS) {
		this.clientOS = clientOS;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public boolean isDebuggerVersion() {
		return isDebuggerVersion;
	}
	public void setDebuggerVersion(boolean isDebuggerVersion) {
		this.isDebuggerVersion = isDebuggerVersion;
	}
	public String getBrowserVersion() {
		return browserVersion;
	}
	public void setBrowserVersion(String browserVersion) {
		this.browserVersion = browserVersion;
	}
	public String getBrowserAgent() {
		return browserAgent;
	}
	public void setBrowserAgent(String browserAgent) {
		this.browserAgent = browserAgent;
	}
	public void setBrowserName(String browserName) {
		this.browserName = browserName;
	}
}
