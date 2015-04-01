/**
 * 
 */
package com.raritan.tdz.piq.integration;

import org.springframework.integration.MessageChannel;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 * This is the interface to route the incoming message to the correct
 * PowerIQ based on its IPAddress.
 */
public interface PowerIQRouter {
	public MessageChannel resolve(String hostName) throws DataAccessException;
	public void reloadCommunicationSettings(ApplicationSettings newApplicationSettings) throws DataAccessException;
	public void remove(String hostName) throws DataAccessException;
}
