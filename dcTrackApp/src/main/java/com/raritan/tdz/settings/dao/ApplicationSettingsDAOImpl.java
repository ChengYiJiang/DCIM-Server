package com.raritan.tdz.settings.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.ApplicationSetting;

public class ApplicationSettingsDAOImpl extends DaoImpl<ApplicationSetting>
		implements ApplicationSettingsDAO {

	/**
	 * get the application setting for a given app setting lks in a given location
	 */
	@Override
	public ApplicationSetting getAppSetting(Long settingLkp, Long locationId) {
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(this.type);
		
		criteria.createAlias("lksData", "lksData");
		criteria.add(Restrictions.eq("lksData.lkpValueCode", settingLkp));
		
		criteria.add(Restrictions.eq("locationId", locationId));
		
		return (ApplicationSetting) criteria.uniqueResult();
		
	}
	
	/**
	 * get the list of application settings for a given lkp type in a given location
	 * @param settingLkpTypeName
	 * @param locationId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ApplicationSetting> getAppSettings(String settingLkpTypeName, Long locationId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(this.type);
		
		criteria.createAlias("lksData", "lksData");
		criteria.add(Restrictions.eq("lksData.lkpTypeName", settingLkpTypeName));
		
		criteria.add(Restrictions.eq("locationId", locationId));
		
		return criteria.list();
		
	}
	
	/**
	 * get a list of application settings for a given lkp type
	 * @param settingLkpTypeName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ApplicationSetting> getAppSettings(String settingLkpTypeName) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(this.type);
		
		criteria.createAlias("lksData", "lksData");
		criteria.add(Restrictions.eq("lksData.lkpTypeName", settingLkpTypeName));
		
		return criteria.list();
		
	}

}
