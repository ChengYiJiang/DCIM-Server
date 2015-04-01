package com.raritan.tdz.snmp.home;

import org.apache.log4j.Logger;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;


/**
 * SNMP Settings implementation.
 */
public class SnmpSettingsHomeImpl implements SnmpSettingsHome {
	
	private final Logger log = Logger.getLogger( this.getClass() );
	
	public SnmpSettingsHomeImpl() {
	}

	@Override
	public void configureSNMP(ApplicationSettings appSettings) throws DataAccessException {
		log.debug("=== configureSNMP invoked");
		try {
			log.debug("snmp enabled: " + appSettings.getBooleanProperty(Name.SNMP_V1V2_ENABLED));
			log.debug("snmp roComunity: " + appSettings.getProperty(Name.SNMP_READ_COMMUNITY_STRING));
			log.debug("snmp sysContact: " + appSettings.getProperty(Name.SNMP_SYS_CONTACT));
			log.debug("snmp sysName: " + appSettings.getProperty(Name.SNMP_SYS_NAME));
			log.debug("snmp sysLocation : " + appSettings.getProperty(Name.SNMP_SYS_LOCATION));
			
			if(!Boolean.parseBoolean(System.getProperty("dcTrack.developer"))){
				log.debug("calling config_snmp.pl to save SNMP configuration");
				//for production...
				Runtime.getRuntime().exec("/usr/bin/sudo /usr/local/sbin/config_snmp.pl");
			}
		}catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.SNMP_CONFIG_UPDATE_FAILED, this.getClass(), t));
		}
	}

	@Override
	public int testSNMPSettings(boolean v1v2Enabled, boolean v3Enabled,
			String readCommunityString, String username, String password) {
		log.info("test SNMP: no tests - returning OK");
		//Cannot be tested from this function
		return 0;
	}
}
