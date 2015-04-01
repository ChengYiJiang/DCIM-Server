package com.raritan.tdz.settings.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.ApplicationSetting;

public interface ApplicationSettingsDAO extends Dao<ApplicationSetting> {

	/**
	 * get the settings value for a given setting lkp in a given location
	 * @param settingLkp
	 * @param locationId
	 * @return
	 */
	public ApplicationSetting getAppSetting(Long settingLkp, Long locationId);

	/**
	 * get the list of application settings for a given lkp type in a given location
	 * @param settingLkpTypeName
	 * @param locationId
	 * @return
	 */
	public List<ApplicationSetting> getAppSettings(String settingLkpTypeName, Long locationId);

	/**
	 * get a list of application settings for a given lkp type
	 * @param settingLkpTypeName
	 * @return
	 */
	public List<ApplicationSetting> getAppSettings(String settingLkpTypeName);
	
	
}
