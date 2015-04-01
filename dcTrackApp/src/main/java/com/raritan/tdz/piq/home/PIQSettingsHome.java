package com.raritan.tdz.piq.home;

import java.util.Map;

import org.springframework.integration.annotation.Payload;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.settings.dto.PiqSettingDTO;
import com.raritan.tdz.util.ApplicationCodesEnum;

/**
 * Home bean for validating and testing PIQ settings.
 * 
 * @author Andrew Cohen
 */
public interface PIQSettingsHome {
	
	/**
	 * Initialize the settings home. This will actually help in initializing the individual
	 * ApplicatoinSettings for AssetStrip so that Asset Strip threads can start.
	 * @param piqHost
	 * @throws DataAccessException
	 */
	public void initalize(String piqHost) throws DataAccessException;

	/**
	 * Re-initializes all PIQ Rest client implementations and scheduled PIQ jobs.
	 * based on current configuration settings in the database.
	 * @param piqHost TODO
	 * @throws DataAccessException
	 */
	public void reloadSettings(String piqHost) throws DataAccessException;
	
	/**
	 * Test the given connection settings.
	 * @param piqHost the hostname or IP Address of the PIQ VM.
	 * @param username the PIQ username 
	 * @param password the PIQ password
	 * @throws ServiceLayerException
	 */
	public ApplicationCodesEnum testSettings(String piqHost, String username, String password) throws DataAccessException;			
	
}
