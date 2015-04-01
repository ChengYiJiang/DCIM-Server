package com.raritan.tdz.settings.home;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.ApplicationSetting;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * Business object for managing global application settings for dcTrack.
 * 
 * @author Andrew Cohen
 */
@Transactional(rollbackFor = DataAccessException.class)
public interface ApplicationSettings {

	/**
	 * Creates or updates an application setting property. 
	 * @param name the setting name
	 * @param value the setting value
	 */
	public void setProperty(Name name, String value) throws DataAccessException;
	
	/**
	 * Gets a specific application setting value.
	 * @param name the setting name
	 * @return the setting value or null if it doesn't exist.
	 */
	public String getProperty(Name name) throws DataAccessException;
	
	/**
	 * Gets a specific application setting value.
	 * @param propertyName the setting name
	 * @return the setting value or null if it doesn't exist.
	 */
	public String getProperty(String name) throws DataAccessException;
	
	/**
	 * Get Property based on appSettingId
	 * @param appSettingId
	 * @return
	 * @throws DataAccessException
	 */
	public String getProperty(Long appSettingId) throws DataAccessException;
	
	/**
	 * Gets a specific application setting value as an integer.
	 * @param name the setting name
	 * @return the integer value or null if is doesn't exist.
	 * @throws DataAccessException
	 */
	public Integer getIntProperty(String name) throws DataAccessException;
	
	public Integer getIntProperty(Name name) throws DataAccessException;
	
	/**
	 * Gets a specific application setting value as a boolean.
	 * @param name the setting name
	 * @return the boolean value 
	 * @throws DataAccessException
	 */
	public boolean getBooleanProperty(String name) throws DataAccessException;
	
	public boolean getBooleanProperty(Name name) throws DataAccessException;
	
	/**
	 * Gets a specific application setting value as a date.
	 * @param name the setting name
	 * @return
	 * @throws DataAccessException
	 */
	public Date getDateProperty(String name) throws DataAccessException;

	public Date getDateProperty(Name name) throws DataAccessException;
	
	/**
	 * Sets a date property.
	 * @param name the setting name
	 * @param value the date value
	 * @throws DataAccessException
	 */
	public void setDateProperty(Name name, Date value) throws DataAccessException;
	
	
	/**
	 * Gets the lookup value code of the LKS value for the setting name.
	 * If the setting has a value but no associated LKS value, it will return null.
	 * @param name the setting name
	 * @return the value code or null
	 * @throws DataAccessException
	 */
	public Long getLkpValueCode(Name name) throws DataAccessException;
	
	/**
	 * Sets the value of the property name to the specified LKS.
	 * @param name the setting name
	 * @param lkpValueCode the lookup value code
	 * @throws DataAccessException
	 */
	public void setPropertyLks(Name name, long lkpValueCode) throws DataAccessException;
	
	/**
	 * Gets settings for a particular location. To get all settings
	 * the location code should be set to null.
	 * @param locationCode optional location code.
	 * @return
	 */
	public List<ApplicationSetting> getSettings(String locationCode) throws DataAccessException;
	
	/**
	 * Retrieves all application settings by lookup type.
	 * @param lkpTypeName the lookup type of the application settings
	 * @return a list of application settings
	 * @throws DataAccessException
	 */
	public List<ApplicationSetting> getSettingsByType(String lkpTypeName) throws DataAccessException;
	
	/**
	 * Retrieves all application settings by powerIQHost
	 * @param lkpTypeName the lookup type of the application settings
	 * @return a list of application settings
	 * @throws DataAccessException
	 */
	public List<ApplicationSetting> getSettingsByPIQHost(String piqHost) throws DataAccessException;
	
	/**
	 * Retrieves all application settings by powerIQLabel
	 * @param lkpTypeName the lookup type of the application settings
	 * @return a list of application settings
	 * @throws DataAccessException
	 */
	public List<ApplicationSetting> getSettingsByPIQLabel(String piqLabel) throws DataAccessException;
	
	/**
	 * Retrieves all powerIQHosts
	 * @param lkpTypeName the lookup type of the application settings
	 * @return a list of application settings
	 * @throws DataAccessException
	 */
	public List<String> getAllPowerIQHosts() throws DataAccessException;
	
	
	/**
	 * Get all powerIQ settings ids
	 * @param excludeSettingTypes TODO
	 * @return
	 * @throws DataAccessException
	 */
	public List<Long> getAllPowerIQSettingIds(List<Long> excludeSettingTypes) throws DataAccessException;
	
	/**
	 * Updates a particular application setting's value.
	 * @param appSettingId the app setting ID
	 * @param value the new value 
	 */
	public void updateSetting(long appSettingId, String value) throws DataAccessException;
	
	/**
	 * Updates a particular application setting's value.
	 * @param appSettingId the app setting ID
	 * @param value the new value 
	 */
	public ApplicationSetting updateSetting(long appSettingId, String value, long parentAppSettingId) throws DataAccessException;
	
	/**
	 * Updates a particular application setting's value.
	 * @param appSettingId the app setting ID
	 * @param value the new value 
	 */
	public ApplicationSetting addSetting(long settingsLkpValueCode, String value, String attribute, String parentValue) throws DataAccessException;
	
	
	/**
	 * Updates a particular application setting's value.
	 * @param settingsLkpValueCode
	 * @param value
	 * @param attribute
	 * @param parentId
	 * @return
	 * @throws DataAccessException
	 */
	public ApplicationSetting addSetting(long settingsLkpValueCode, String value, String attribute, Long parentId) throws DataAccessException;

	
	/**
	 * Updates a particular application setting's value.
	 * @param UI Field
	 * @param attribute 
	 */
	public void updateUiFields(String uiField, String attribute) throws DataAccessException;
	
	/**
	 * Set the PowerIQHost so that this application setting's getProperty gives you the 
	 * values for the given powerIQ host.
	 * @param powerIQHost
	 */
	public void setPowerIQHost(String powerIQHost);
	
	/**
	 * Get current PowerIQ Host.
	 * @return
	 */
	public String getPowerIQHost();
	
	/**
	 * Delete all the powerIQSettings
	 * @throws DataAccessException 
	 */
	public void deleteAllPowerIQSettings() throws DataAccessException;
	
	/**
	 * Delete powerIQSettings for the given hosts
	 * @param piqHosts
	 */
	public void deletePowerIQSettings(List<String> piqHosts) throws DataAccessException;
	
	/**
	 * Delete powerIQSettings for the given hosts
	 * @param piqHosts
	 * @return List of powerIQ hosts deleted
	 */
	public List<String> deletePowerIQSettings(List<Long> piqHosts, Errors errors) throws DataAccessException;
	
	/**
	 *  The list of all application settings.
	 */
	public enum Name {
		PIQ_IPADDRESS {
			public long valueCode() { return SystemLookup.ApplicationSettings.PIQ_IPADDRESS; }
		},
		PIQ_USERNAME {
			public long valueCode() { return SystemLookup.ApplicationSettings.PIQ_USERNAME; }
		},
		PIQ_PASSWORD {
			public long valueCode() { return SystemLookup.ApplicationSettings.PIQ_PASSWORD; }
		},
		PIQ_POLLING_INTERVAL {
			public long valueCode() { return SystemLookup.ApplicationSettings.PIQ_POLLING_INTERVAL; }
		},
		PIQ_POLLING_ENABLED {
			public long valueCode() { return SystemLookup.ApplicationSettings.PIQ_POLLING_ENABLED; }
		},
		PIQ_EVENT_QUERY_DATE {
			public long valueCode() { return SystemLookup.ApplicationSettings.PIQ_EVENT_QUERY_DATE; }
		},
		PIQ_VERSION {
			public long valueCode() { return SystemLookup.ApplicationSettings.PIQ_VERSION; }
		},
		SSL_CLIENT_TRUSTLEVEL {
			public long valueCode() { return SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTLEVEL; }
		},
		SNMP_V1V2_ENABLED {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_V1V2_ENABLED; }
		},
		SNMP_V3_ENABLED {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_V3_ENABLED; }
		},
		SNMP_READ_COMMUNITY_STRING {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_READ_COMMUNITY_STRING; }
		},
		SNMP_WRITE_COMMUNITY_STRING {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_WRITE_COMMUNITY_STRING; }
		},
		SNMP_USERNAME {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_USERNAME; }
		},
		SNMP_PASSWORD {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_PASSWORD; }
		},
		SNMP_SYS_CONTACT {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_SYS_CONTACT; }
		},
		SNMP_SYS_NAME {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_SYS_NAME; }
		},
		SNMP_SYS_LOCATION {
			public long valueCode() { return SystemLookup.ApplicationSettings.SNMP_SYS_LOCATION; }
		},
		VPC_ENABLED {
			public long valueCode() { return SystemLookup.ApplicationSettings.VPC_SETTINGS.ENABLED; }
		}
		
		;
		public abstract long valueCode();
	}		
}
