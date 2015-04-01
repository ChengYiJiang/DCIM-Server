/**
 * 
 */
package com.raritan.tdz.piq.home;

import org.apache.log4j.Logger;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public abstract class PIQSyncBase extends PIQRestClientBase {
	
	public PIQSyncBase(ApplicationSettings appSettings)
			throws DataAccessException{
		super(appSettings);
	}
	
	// Logger for all PIQ sync operations
	protected Logger log = Logger.getLogger("PIQSyncLogger");
	
	/**
	 * @param e
	 */
	protected void logError(RemoteDataAccessException e) {
		if (log.isDebugEnabled())
			e.printStackTrace();
		else
			log.error("REST call failed: " + e.getMessage());
	}
}
