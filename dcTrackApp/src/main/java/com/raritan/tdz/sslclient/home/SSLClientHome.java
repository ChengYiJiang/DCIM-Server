package com.raritan.tdz.sslclient.home;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;


/**
 * This bean controls the SSL certificate checking and authentication that dcTrack will perform as a CLIENT.
 * 
 * @author Andrew Cohen
 */
public interface SSLClientHome {
	
	/**
	 * Sets the SSL client trust level.
	 * @param trustLevel the trust level 
	 */
	public void setTrustLevel(TrustLevel trustLevel) throws DataAccessException;
	
	/**
	 * Return the current trust level.
	 * @return
	 */
	public TrustLevel getTrustLevel() throws DataAccessException;
	
	/**
	 * Supported Trust Levels.
	 */
	public enum TrustLevel {
		/** Trust all certificates - does not perform any validation of the server certificate */
		TRUST_ALL {
			 public long valueCode() { return SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTALL_OPTION; }
		},
		/** Trust only those server certificates that are in the dcTrack client trustStore */
		TRUST_STORE {
			 public long valueCode() { return SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTSTORE_OPTION; }
		},
		/** Same as TRUST_STORE but also verifies that the server host name matches the common name in the certificate */
		TRUST_STORE_AND_VERIFY_HOSTNAME {
			 public long valueCode() { return SystemLookup.ApplicationSettings.SSL_CLIENT_TRUSTSTORE_AND_HOST_OPTION; }
		};
		public abstract long valueCode();
	};
}