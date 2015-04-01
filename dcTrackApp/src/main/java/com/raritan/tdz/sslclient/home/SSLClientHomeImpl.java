package com.raritan.tdz.sslclient.home;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;

/**
 * A common utility for performing SSL related functions.
 * 
 * @author Andrew Cohen
 */
public class SSLClientHomeImpl implements SSLClientHome {
	
	private final Logger log = Logger.getLogger( SSLClientHome.class );
	
	private ApplicationSettings appSettings;
	
	public SSLClientHomeImpl(ApplicationSettings appSettings) throws DataAccessException {
		this.appSettings = appSettings;
		this.initTrustLevel();
	}
	
	@Override
	public void setTrustLevel(TrustLevel trustLevel) throws DataAccessException {
		appSettings.setPropertyLks(Name.SSL_CLIENT_TRUSTLEVEL, trustLevel.valueCode());
		initTrustLevel();
	}
	
	@Override
	public TrustLevel getTrustLevel() throws DataAccessException {
		Long value = appSettings.getLkpValueCode( Name.SSL_CLIENT_TRUSTLEVEL );
		
		if (value == null) {
			value = SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTALL_OPTION;
		}
		
		if (value == SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTALL_OPTION) {
			return TrustLevel.TRUST_ALL;
		}
		else if (value == SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTSTORE_OPTION) {
			return TrustLevel.TRUST_STORE;
		}
		else if (value == SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTSTORE_AND_HOST_OPTION) {
			return TrustLevel.TRUST_STORE_AND_VERIFY_HOSTNAME;
		}
		
		return null;
	}
	
	//
	// Private methods
	//
	
	private void trustCertificates(boolean verifyHostName) {
		// For httpclient 3.1
		ClientSSLProtocolSocketFactory socketFactory = new ClientSSLProtocolSocketFactory(
			"raritan", // Keystore password
			"raritan" // Truststore password
		);
		socketFactory.setHostnameVerification( verifyHostName );
		Protocol p = new Protocol("https", (ProtocolSocketFactory)socketFactory, 443);
		Protocol.registerProtocol("https", p);
	}
	
	private void trustAllCertificates() {
		// For httpclient 3.1
		Protocol p = new Protocol("https", (ProtocolSocketFactory)new AcceptAllCertsProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", p);
		
		// Install the all-trusting trust manager
		TrustManager[] trustAllCerts = new TrustManager[] { new AcceptAllCertsTrustManager() };
		SSLContext sc = null;
		
		try {
			sc = SSLContext.getInstance("SSL");
		}
		catch (NoSuchAlgorithmException e) {
			return;
		}
		
		try {
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
		} 
		catch (KeyManagementException e) {
			log.error("", e);
			return;
		}
		
		HttpsURLConnection
				.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection
		.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String string, SSLSession ssls) {
				return true;
			}
		});
	}
	
	private void initTrustLevel() throws DataAccessException {
		TrustLevel trustLevel = getTrustLevel();
		switch ( trustLevel ) {
			case TRUST_ALL:
				trustAllCertificates();
				break;
			case TRUST_STORE:
				trustCertificates( false );
				break;
			case TRUST_STORE_AND_VERIFY_HOSTNAME:
				trustCertificates( true );
				break;
		}
	}
}
