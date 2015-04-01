/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.ApplicationSetting;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.settings.dto.ApplicationSettingDTO;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQAssetStripEventBoot {
	PIQSettingsHome piqSettingHome;
	ApplicationSettings settingsHome;
	private static String lkpTypeName = SystemLookup.ApplicationSettings.TypeName.PIQ_SETTING;
	
	
	public PIQAssetStripEventBoot(PIQSettingsHome piqSettingHome, ApplicationSettings settingsHome){
		this.piqSettingHome = piqSettingHome;
		this.settingsHome = settingsHome;
	}
	
	public void init() throws DataAccessException{
		List<String> piqHosts = settingsHome.getAllPowerIQHosts();
		for (String piqHost:piqHosts){
			if (!piqHost.isEmpty())
				piqSettingHome.initalize(piqHost);
		}
	}
}
