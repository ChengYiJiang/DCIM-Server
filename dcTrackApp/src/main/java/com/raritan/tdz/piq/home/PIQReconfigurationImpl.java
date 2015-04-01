/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.ArrayList;
import java.util.List;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;

/**
 * @author prasanna
 *
 */
public class PIQReconfigurationImpl implements PIQReconfiguration {
	List<PIQRestClient> piqRestClients = new ArrayList<PIQRestClient>();
	
	public PIQReconfigurationImpl(List<PIQRestClient> piqRestClients){
		this.piqRestClients = piqRestClients;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQReconfiguration#reloadCommunicationSettings(com.raritan.tdz.settings.home.ApplicationSettings)
	 */
	@Override
	public void reloadCommunicationSettings(ApplicationSettings appSettings)
			throws DataAccessException {
		String ipAddr = appSettings.getProperty( Name.PIQ_IPADDRESS );
		String username = appSettings.getProperty( Name.PIQ_USERNAME );
		String password = appSettings.getProperty( Name.PIQ_PASSWORD );
		
		if (ipAddr != null && username != null && password != null){
			for (PIQRestClient piqRestClient: piqRestClients) {
				 piqRestClient.setIPAddress( ipAddr );
				 piqRestClient.setCredentials( username, password );
			}
		}

	}

}
