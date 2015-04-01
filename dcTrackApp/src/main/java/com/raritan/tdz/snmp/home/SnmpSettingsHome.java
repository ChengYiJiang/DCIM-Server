package com.raritan.tdz.snmp.home;

import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.exception.DataAccessException;
/**
 * This bean controls SNMP configuration.
 */ 
public interface SnmpSettingsHome {
	
	/**
	 * Configure SNMP from the application settings.
	 * @param appSettings
	 */
	public void configureSNMP(ApplicationSettings appSettings) throws DataAccessException;
	
	/**
	 * Test the given SNMP settings.
	 * @param v1v2Enabled true if SNMP v1/v2 enabled
	 * @param v3Enabled true if SNMP v3 enabled
	 * @param readCommunityString the read community string
	 * @param username SNMPusername for v3
	 * @param password SNMP password for v3
	 * @return the application error or success code
	 */
	public int testSNMPSettings(boolean v1v2Enabled, 
			boolean v3Enabled,
			String readCommunityString,
			String username,
			String password);
}