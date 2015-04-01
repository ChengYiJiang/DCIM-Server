package com.raritan.tdz.piq.home;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.quartz.impl.StdScheduler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.Payload;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * PIQ Settings Control implementation.
 * 
 * @author Andrew Cohen
 */
public class PIQSettingsHomeImpl implements PIQSettingsHome {

	private final Logger log = Logger.getLogger( PIQSettingsHome.class );
	
	/** The asset strip job trigger - its repeat interval will be updated with the current setting */
	private SimpleTriggerBean assetStripJobTrigger;
	
	/** The asset strip scheduler - it will be rescheduled or stopped based on the Polling enabled setting */
	private StdScheduler assetStripScheduler;
	
	private RestTemplate restTemplate;
	
	private ApplicationSettings applicationSettings;
	private PIQReconfiguration piqReconfiguration;
	
	
	public PIQSettingsHomeImpl(SimpleTriggerBean assetStripJobTrigger, 
			StdScheduler assetStripScheduler,
			RestTemplate restTemplate, ApplicationSettings applicationSettings,
			PIQReconfiguration piqReconfiguration) {
		this.assetStripJobTrigger = assetStripJobTrigger;
		this.assetStripScheduler = assetStripScheduler;
		this.restTemplate = restTemplate;
		this.applicationSettings = applicationSettings;
		this.piqReconfiguration = piqReconfiguration;
	}
	
	@Override
	public void reloadSettings(@Payload("#this[0]") String piqHost) throws DataAccessException {
		
		// First, reload PIQ communication settings for PIQ REST clients
		//PIQRestClientBase.reloadCommunicationSettings( settings );
		applicationSettings.setPowerIQHost(piqHost);
		piqReconfiguration.reloadCommunicationSettings(applicationSettings);
		
		// Second, check the settings to determine if polling should be enabled or disabled
//		String host = settings.getProperty( Name.PIQ_IPADDRESS );
//		String username = settings.getProperty( Name.PIQ_USERNAME );
//		String password = settings.getProperty( Name.PIQ_PASSWORD );
//		boolean pollingEnabled = StringUtils.hasText(host) &&
//			StringUtils.hasText(username) &&
//			StringUtils.hasText(password);
//		settings.setProperty(Name.PIQ_POLLING_ENABLED, Boolean.toString(pollingEnabled));
		
		assetStripScheduler.standby();
		
		if (applicationSettings.getBooleanProperty( Name.PIQ_POLLING_ENABLED )) {
			// The following is not needed because we can't change the polling interval from the GUI
			/*
			String triggerName = assetStripJobTrigger.getName();
			String groupName = assetStripJobTrigger.getGroup();
			int pollIntervalMinutes = settingsHome.getIntProperty( Name.PIQ_POLLING_INTERVAL );
			
			assetStripJobTrigger.setRepeatInterval( pollIntervalMinutes * 60000 );
			assetStripJobTrigger.setStartDelay(0);
			*/
			
			try {
				// The following line is not needed because we can't change the polling interval from the GUI
				//assetStripScheduler.rescheduleJob(triggerName, groupName, assetStripJobTrigger);
				assetStripScheduler.start();
			}
			catch (SchedulerException e) {
				throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.JOB_SCHEDULER_FAILED, this.getClass(), e));
			}
		}
	}

	@Override
	public ApplicationCodesEnum testSettings(@Payload("#this[0]") String host, @Payload("#this[1]") String username, @Payload("#this[2]") String password) throws DataAccessException {		
		
		InetAddress inet = null;
		
		// Check PIQ REST connectivity first - if that passes, no other tests should be performed
		PIQRestTest rest = new PIQRestTest(host, username, password);
		ApplicationCodesEnum restResult = rest.testConnection();
		if (restResult == ApplicationCodesEnum.PIQ_SETTINGS_VALIDATED) {
			// REST OK - now Test ODBC
			ApplicationCodesEnum code = testODBC();
			if (code != ApplicationCodesEnum.PIQ_SETTINGS_VALIDATED) {
				return code;
			}
			return ApplicationCodesEnum.PIQ_SETTINGS_VALIDATED;
		}
		
		// REST or ODBC failed - next validate hostname or IP Address
		try {
			inet = InetAddress.getByName( host );
		}
		catch (UnknownHostException e) {
			log.warn("" , e);
			return ApplicationCodesEnum.PIQ_HOSTNAME_INVALID;
		}
		
		// Lastly, check that the address is reachable
		try {
			if (!inet.isReachable(20000)) {
				return ApplicationCodesEnum.PIQ_HOSTNAME_UNREACHABLE;
			}
		}
		catch (IOException e) {
			log.warn("" , e);
			return ApplicationCodesEnum.PIQ_HOSTNAME_UNREACHABLE;
		}
		
		return restResult;
	}
	
	
	
	@Override
	public void initalize(@Payload("#this[0]") String piqHost) throws DataAccessException {
		//We need to reloadSettings so that the assetStrip Event threads get started for a specific PowerIQ		
		reloadSettings(piqHost);
	}
	
	private ApplicationCodesEnum testODBC() {
//	    try {
//	    	Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
//	    	DriverManager.getConnection("jdbc:odbc:raritan", "", "");
//	    }
//	    catch (Throwable t) {
//	    	t.printStackTrace();
//	    	return ApplicationCodesEnum.PIQ_ODBC_FAILED;
//	    }
		
	    return ApplicationCodesEnum.PIQ_SETTINGS_VALIDATED;
	}
	
	/**
	 * Simple PIQ REST client for testing connectivity. 
	 * This client will NOT log failures in the system event log.
	 */
	private class PIQRestTest extends PIQRestClientBase {

		public PIQRestTest(String host, String username, String password) {
			setIPAddress( host );
			setCredentials(username, password);
		}
		
		public ApplicationCodesEnum testConnection() {
			String url = getRestURL("v2/asset_strips", null);
			try {
				restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>( getHttpHeaders() ), Map.class);
			}
			catch (ResourceAccessException e) {
				return ApplicationCodesEnum.PIQ_NOT_RUNNING;
			}
			catch (HttpClientErrorException e) {
				return ApplicationCodesEnum.PIQ_CREDENTIALS_INVALID;
			}
			catch (HttpServerErrorException e) {
				return ApplicationCodesEnum.PIQ_SERVICE_FAILED;
			}
			catch (RestClientException e) {
				// It's possible that some service other than PIQ is listening
				// and responded with some unexpected JSON.
				return ApplicationCodesEnum.PIQ_NOT_RUNNING;
			}
			catch (Throwable t) {
				return ApplicationCodesEnum.PIQ_SERVICE_FAILED;
			}
			
			return ApplicationCodesEnum.PIQ_SETTINGS_VALIDATED;
		}
		
			}


}
