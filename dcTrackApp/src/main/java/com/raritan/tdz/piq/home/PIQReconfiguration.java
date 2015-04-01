/**
 * 
 */
package com.raritan.tdz.piq.home;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public interface PIQReconfiguration {
	public void reloadCommunicationSettings(ApplicationSettings appSettings) throws DataAccessException;
}
