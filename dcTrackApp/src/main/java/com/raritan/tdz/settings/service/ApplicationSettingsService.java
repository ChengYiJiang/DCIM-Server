package com.raritan.tdz.settings.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.piq.dto.PIQBulkSyncStatusDTO;
import com.raritan.tdz.settings.dto.ApplicationSettingDTO;
import com.raritan.tdz.settings.dto.PiqSettingDTO;

/**
 * Service for managing dcTrack application settings.
 * 
 * @author Andrew Cohen
 */
public interface ApplicationSettingsService {

	/**
	 * Retrieve the list of PIQ configuration settings.
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public List<ApplicationSettingDTO> getPIQSettings() throws ServiceLayerException;
	
	/**
	 * Retrieve the list of PIQ configuration settings for a given powerIQHost
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public List<ApplicationSettingDTO> getPIQSettings(String piqHost) throws ServiceLayerException;
	
	
	/**
	 * Updates PIQ configuration settings.
	 * @param appSettings the list of application settings
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public int updatePIQSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException;
	
	/**
	 * Delete a PowerIQ setting given a PowerIQ host addresses.
	 * @param piqHosts
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public List<ApplicationSettingDTO> deletePIQSettings(List<String> piqHosts) throws ServiceLayerException;
	
	/**
	 * Delete a PowerIQ setting given a set of application settings using PowerIQ host record IDs.
	 * @param piqHostIds - These must be powerIQ Host Application Setting Ids only. If not a business validation exception is thrown
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public List<ApplicationSettingDTO> deletePIQSettingsUsingHostIds(List<Integer> piqHostIds) throws ServiceLayerException;
	
	/**
	 * Start the process of updating Power IQ with all items in dcTrack.
	 * @param piqHost TODO
	 * @throws ServiceLayerException
	 */
	public PIQBulkSyncStatusDTO updatePIQData(String piqHost) throws ServiceLayerException;
	
	/**
	 * Get status on Power IQ data synchronization.
	 * @param piqHost TODO
	 * @return
	 */
	public PIQBulkSyncStatusDTO getPIQUpdateDataStatus(String piqHost) throws ServiceLayerException;
	
	/**
	 * Stops the currently running PIQ update job.
	 * @param piqHost TODO
	 * @throws ServiceLayerException
	 */
	public PIQBulkSyncStatusDTO stopPIQDataUpdate(String piqHost) throws ServiceLayerException;
	
	/**
	 * This will tell the client if there is a powerIQ update already running with any of the 
	 * configured PowerIQs.
	 * @return
	 * @throws ServiceLayerException
	 */
	public Boolean isPIQUpdatedRunning() throws ServiceLayerException;
	
	/**
	 * Test the given PIQ connection settings.
	 * @param piqHost the hostname or IP Address of the PIQ VM
	 * @param username the PIQ username
	 * @param password the PIQ password
	 * @return the application code for the result of the test
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public int testPIQSettings(String piqHost, String username, String password) throws ServiceLayerException;
	
	/**
	 * CR56619 Populate PIQ Version
	 * Test the given PIQ connection settings.
	 * @param piqHost the hostname or IP Address of the PIQ VM
	 * @param username the PIQ username
	 * @param password the PIQ password
	 * @return the application code for the result of the test
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)	
	public PiqSettingDTO testPIQSettingsAndGetVersion(PiqSettingDTO piqSettingDTO) throws ServiceLayerException;
	
	/**
	 * Retrieve the list of SNMP configuration settings.
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public List<ApplicationSettingDTO> getSNMPSettings() throws ServiceLayerException;
	
	/**
	 * Updates SNMP configuration settings.
	 * @param appSettings the list of application settings
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public int updateSNMPSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException;

	/**
	 * Retrieve the list of VPC configuration settings.
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public List<ApplicationSettingDTO> getVPCSettings() throws ServiceLayerException;
	
	/**
	 * Updates VPC configuration settings.
	 * @param appSettings the list of application settings
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public int updateVPCSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException;
	

	/**
	 * Tests the given SNMP settings.
	 * @param appSettings
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public int testSNMPSettings(boolean v1v2Enabled, boolean v3Enabled, String readCommunity, String username, String password) throws ServiceLayerException;
	
	/**
	 * Gets all application settings.
	 * @return
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public List<ApplicationSettingDTO> getApplicationSettings() throws ServiceLayerException;
	
	/**
	 * Get all applications settings for a particular location.
	 * @param locationCode location code. if null, returns all settings.
	 * @return
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public Collection<ApplicationSettingDTO> getApplicationSettings(String locationCode) throws ServiceLayerException;
	
	/**
	 * This method is provided solely for testing business exception handling.
	 * It is guaranteed to throw a BusinessValidationException.
	 */
	public void testBusinessException() throws ServiceLayerException;
	
	/**
	 *  This method is provided solely for testing system exception handling.
	 *   It is guaranteed to throw a SystemException.
	 */
	public void testSystemException() throws ServiceLayerException;
	


	/**
	 * Get .
	 * @param appSettings the list of application settings
	 * @return
	 * @throws ServiceLayerException
	 */
	public Map<String, UiComponentDTO> getUiFieldsInitState() throws ServiceLayerException;
	
	/**
	 * Updates fields attribute use in web forms.
	 * @param appSettings the list of application settings
	 * @return
	 * @throws ServiceLayerException
	 */
	@Transactional(rollbackFor = ServiceLayerException.class)
	public int updateRequiredUiFields(List<ValueIdDTO> appSettings) throws ServiceLayerException;

	/**
	 * Check if VPC is enable for a location.
	 * @param Location Code
	 * @return true or false
	 * @throws ServiceLayerException
	 */
	boolean isVPCEnabledForLocation(String locationCode) throws ServiceLayerException;
	
}
