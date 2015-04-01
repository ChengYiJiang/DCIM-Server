package com.raritan.tdz.piq.home;


/**
 * Base interface for specifying a PIQ rest service.
 * 
 * @author Andrew Cohen
 */
public interface PIQRestClient {
	
	// The source for events that the REST client creates
	public static final String EVENT_SOURCE = "Power IQ";
	
	// Date format returned in PIQ responses
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss Z";
	
	/**
	 * Set the username and password credentials used for authorization.
	 * @param username
	 * @param password
	 */
	public void setCredentials(String username, String password);
	
	/**
	 * Set the IP address of the PIQ system.
	 * @param ipAddress
	 */
	public void setIPAddress(String ipAddress);
	
	/**
	 * Returns the relative name of the REST service. (Example: "v2/events")
	 * @return
	 */
	public String getService();
	
	/**
	 * Sets  the relative name of the REST service. (Example: "v2/rack_units")
	 * @param service
	 */
	public void setService(String service);
	
	/**
	 * Returns true if the appSettings is enabled
	 * @return
	 */
	public boolean isAppSettingsEnabled();
	
	/**
	 * Returns the event source for this rest client
	 * @return
	 */
	public String getEventSource();

	/**
	 * return the IPAddress configured for communicating with PIQ 
	 * @return
	 */
	String getIpAddress();
}
