package com.raritan.tdz.settings.home;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.ApplicationSetting;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Application Settings service implementation using database.
 * 
 * @author Andrew Cohen
 */
public class ApplicationSettingsImpl implements ApplicationSettings {

	/** Internal format for dates */
	private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	
	private SessionFactory sessionFactory;
	
	private String powerIQHost;
	
	private long piqHostAppSettingId = -1;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupDAO;
	
	public ApplicationSettingsImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public String getPowerIQHost() {
		return powerIQHost;
	}
	
	/**
	 * This is used by the application context specific AppSettings bean so that
	 * this DAO can retrieve PowerIQ specific configurations.
	 * @param powerIQHost
	 */
	@Override
	@Transactional
	public void setPowerIQHost(String powerIQHost) {	
			this.powerIQHost = powerIQHost;
			//Get the powerIQ Host application setting id from the database
			piqHostAppSettingId = getPIQHostAppSettingId();
	}

	public long getPiqHostAppSettingId() {
		return piqHostAppSettingId;
	}

	@Override
	public void setProperty(Name name, String value) throws DataAccessException {
		Session session = null;
		ApplicationSetting setting = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			setting = getAppSettingForName( session, name );
			
			if (setting != null) {
				// Update existing setting
				setting.setValue( value );
				session.merge( setting );
			}
			else {
				// Add a new setting
				setting = new ApplicationSetting();
				setting.setLksData( SystemLookup.getLksData(session, name.valueCode()) );
				setting.setValue( value );
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATION_SETTING_UPDATE_FAILED, this.getClass(), t));
		}
	}

	@Override
	public String getProperty(Name name) throws DataAccessException {
		if (name == null) return null;
		Session session = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			String value = getAppSettingValueForName(session, name);
			
			if (value != null && value != "") {
				return value;
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATIONS_SETTING_FETCH_FAILED, this.getClass(), t));
		}
		
		return null;
	}
	
	@Override
	public String getProperty(Long appSettingId) throws DataAccessException {
		if (appSettingId == null) return null;
		
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(ApplicationSetting.class);
			criteria.setProjection(Projections.property("value"));
			criteria.add(Restrictions.eq("id", appSettingId));
			
			
			String value = (String) criteria.uniqueResult();
			
			if (value != null && value != "") {
				return value;
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATIONS_SETTING_FETCH_FAILED, this.getClass(), t));
		}
		
		return null;
	}	
	
	@Override
	public String getProperty(String propertyName) throws DataAccessException {
		Name name = null;
		try {
			name = Name.valueOf( propertyName );
		}
		catch (Throwable t) {
			name = null;
		}
		return getProperty( name );
	}
	
	@Override
	public Integer getIntProperty(String propertyName) throws DataAccessException {
		Integer intVal = null;
		String value = getProperty(propertyName);
		
		if (value != null) {
			try {
				intVal = Integer.parseInt( value );
			}
			catch (NumberFormatException e) {
				intVal = null;
			}
		}
		
		return intVal;
	}
	
	@Override
	public Integer getIntProperty(Name name) throws DataAccessException {
		Integer intVal = null;
		String value = getProperty(name);
		
		if (value != null) {
			try {
				intVal = Integer.parseInt( value );
			}
			catch (NumberFormatException e) {
				intVal = null;
			}
		}
		
		return intVal;
	}
	
	@Override
	public boolean getBooleanProperty(String propertyName) throws DataAccessException {
		return Boolean.parseBoolean( getProperty(propertyName) );
	}
	
	@Override
	public boolean getBooleanProperty(Name propertyName) throws DataAccessException {
		return Boolean.parseBoolean( getProperty(propertyName) );
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ApplicationSetting> getSettings(String locationCode) throws DataAccessException {
		List<ApplicationSetting> settings = null;
		Session session = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			Criteria c = session.createCriteria(ApplicationSetting.class);
			if (locationCode != null) {
				c.createAlias("location", "location");
				c.add(Restrictions.eq("location.code", locationCode));
			}
			settings = c.list();
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATIONS_SETTING_FETCH_FAILED, this.getClass(), t));
		}
		
		return settings;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ApplicationSetting> getSettingsByType(String lkpTypeName) throws DataAccessException {
		List<ApplicationSetting> settings = null;
		
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria c = session.createCriteria(ApplicationSetting.class);
			c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			c.createAlias("lksData", "lksData");
			c.add(Restrictions.eq("lksData.lkpTypeName", lkpTypeName));
			settings = c.list();
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATIONS_SETTING_FETCH_FAILED, this.getClass(), t));
		}
		 
		if (settings == null)
			settings = new LinkedList<ApplicationSetting>();
		
		return settings;
 	}

	@Override
	public void updateSetting(long appSettingId, String value) throws DataAccessException {
		Session session = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			ApplicationSetting setting = (ApplicationSetting)session.get(ApplicationSetting.class, appSettingId);
			if (setting != null) {
				setting.setValue( value );
				session.merge( setting );
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATION_SETTING_UPDATE_FAILED, this.getClass(), t));
		}
	}
	
	@Override
	public ApplicationSetting updateSetting(long appSettingId, String value,
			long parentAppSettingId) throws DataAccessException {
		Session session = null;
		ApplicationSetting result = null;
		try {
			session = sessionFactory.getCurrentSession();
			ApplicationSetting setting = (ApplicationSetting)session.get(ApplicationSetting.class, appSettingId);
			if (setting != null) {
				setting.setValue( value );
				if (parentAppSettingId > 0){
					setting.setParentAppSettings((ApplicationSetting)session.get(ApplicationSetting.class, parentAppSettingId));
				}
				result = (ApplicationSetting) session.merge( setting );
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATION_SETTING_UPDATE_FAILED, this.getClass(), t));
		}
		return result;
	}

	@Override
	public void updateUiFields(String uiField, String attribute) throws DataAccessException {
		Session session = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			
			ApplicationSetting setting = getAppSettingUiFields(session, uiField);
			
			if (setting != null) {
				setting.setAttribute(attribute);
				session.merge( setting );
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATION_SETTING_UPDATE_FAILED, this.getClass(), t));
		}
	}
	
	@Override
	public List<ApplicationSetting> getSettingsByPIQHost(String piqHost)
			throws DataAccessException {
		
		Session session = sessionFactory.getCurrentSession();
		
		long parentId = getPIQHostAppSettingIdByHost(piqHost);
		
		List<ApplicationSetting> result = new ArrayList<ApplicationSetting>();
		
		ApplicationSetting parentSetting = (ApplicationSetting) session.get(ApplicationSetting.class, parentId);
		if (parentSetting != null)
			result.add(parentSetting);
		
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.createAlias("parentAppSettings", "parentAppSettings");
		c.add(Restrictions.eq("parentAppSettings.id", parentId));
		
		result.addAll(c.list());
		return result;
	}
	
	@Override
	public List<ApplicationSetting> getSettingsByPIQLabel(String piqLabel)
			throws DataAccessException {
		
		Session session = sessionFactory.getCurrentSession();
		
		long parentId = getPIQHostAppSettingIdByLabel(piqLabel);
		
		List<ApplicationSetting> result = new ArrayList<ApplicationSetting>();
		
		ApplicationSetting parentSetting = (ApplicationSetting) session.get(ApplicationSetting.class, parentId);
		if (parentSetting != null)
			result.add(parentSetting);
		
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.createAlias("parentAppSettings", "parentAppSettings");
		c.add(Restrictions.eq("parentAppSettings.id", parentId));
		
		result.addAll(c.list());
		return result;
	}
	

	@Override
	public List<String> getAllPowerIQHosts() throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		c.createAlias("lksData", "lksData");
		c.add(Restrictions.eq("lksData.lkpValueCode", SystemLookup.ApplicationSettings.PIQ_IPADDRESS));
		c.setProjection(Projections.property("value"));
		return c.list();
	}

	@Override
	public List<Long> getAllPowerIQSettingIds(List<Long> excludeSettingTypes) throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		c.createAlias("lksData", "lksData");
		c.add(Restrictions.eq("lksData.lkpTypeName", SystemLookup.ApplicationSettings.TypeName.PIQ_SETTING));
		for (Long excludeSettingType:excludeSettingTypes)
			c.add(Restrictions.ne("lksData.lkpValueCode", excludeSettingType));
		c.setProjection(Projections.id());
		return c.list();
	}
	
	
	public void removeProperty(Name name) throws DataAccessException {
		Session session = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			Query q = session.createQuery("delete from ApplicationSetting where lksData.lkpValueCode = :valueCode");
			q.setLong("valueCode", name.valueCode());
			q.executeUpdate();
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATION_SETTING_UPDATE_FAILED, this.getClass(), t));
		}
	}
	
	@Override
	public Long getLkpValueCode(Name name) throws DataAccessException {
		if (name == null) return null;
		Session session = null;
		ApplicationSetting setting = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			setting = getAppSettingForName(session, name);
			
			if (setting != null) {
				LksData lks = setting.getValueLks();
				if (lks != null) {
					return lks.getLkpValueCode();
				}
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATIONS_SETTING_FETCH_FAILED, this.getClass(), t));
		}
		
		return null;
	}
	
	public void setPropertyLks(Name name, long lkpValueCode) throws DataAccessException {
		Session session = null;
		ApplicationSetting setting = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			setting = getAppSettingForName( session, name );
			
			if (setting != null) {
				// Update existing setting
				setting.setValueLks( SystemLookup.getLksData(session, lkpValueCode) );
				session.merge( setting );
			}
			else {
				// Add a new setting
				setting = new ApplicationSetting();
				setting.setLksData( SystemLookup.getLksData(session, name.valueCode()) );
				setting.setValueLks( SystemLookup.getLksData(session, lkpValueCode) );
				
				//Set the parent here for any new records added.
				if (!name.equals(Name.PIQ_IPADDRESS) && piqHostAppSettingId > 0){
					ApplicationSetting parentAppSetting = (ApplicationSetting) session.get(ApplicationSetting.class, piqHostAppSettingId);
					setting.setParentAppSettings(parentAppSetting);
				}
			}
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATION_SETTING_UPDATE_FAILED, this.getClass(), t));
		}
	}
	
	@Override
	public Date getDateProperty(String name) throws DataAccessException {
		String value = getProperty( name );
		if (StringUtils.hasText(value)) {
			return getDate( value );
		}
		return null;
	}

	@Override
	public Date getDateProperty(Name name) throws DataAccessException {
		String value = getProperty( name );
		if (StringUtils.hasText(value)) {
			return getDate( value );
		}
		return null;
	}
	
	@Override
	public void setDateProperty(Name name, Date value) throws DataAccessException {
		setProperty( name, formatDate( value ) );
	}
	
	@Override
	public ApplicationSetting addSetting(long settingsLkpValueCode,
			String value, String attribute, String parentValue)
			throws DataAccessException {
		//Get the parent if given a value
		ApplicationSetting parentApplicationSetting = getParentApplicationSettings(parentValue);
		return addSettingInternal(settingsLkpValueCode, value, attribute,parentApplicationSetting);
	}

	
	@Override
	public ApplicationSetting addSetting(long settingsLkpValueCode,
			String value, String attribute, Long parentId)
			throws DataAccessException {
		ApplicationSetting parentApplicationSetting = getParentApplicationSettings(parentId);
		return addSettingInternal(settingsLkpValueCode, value, attribute,parentApplicationSetting);
	}

	
	@Override
	public void deleteAllPowerIQSettings() throws DataAccessException {
		List<String> piqHosts = getAllPowerIQHosts();
		
		deletePowerIQSettings(piqHosts);
	}

	@Override
	public void deletePowerIQSettings(List<String> piqHosts) throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
	
		updateItemPIQInfo(piqHosts, session);
		
		updateLocationAppSettings(piqHosts, session);
		
		deletePowerIQAttributes(piqHosts, session);
		
		deletePowerIQSettings(piqHosts, session);
	}
	
	
	@Override
	public List<String> deletePowerIQSettings(List<Long> piqHostIds, Errors errors)
			throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
		
		List<String> piqHosts = new ArrayList<>();
		
		for (Long hostId:piqHostIds){
			ApplicationSetting appSetting = (ApplicationSetting) session.get(ApplicationSetting.class, hostId);
			if (appSetting == null || (appSetting.getLksData() != null && appSetting.getLksData().getLkpValueCode() != SystemLookup.ApplicationSettings.PIQ_IPADDRESS))
			{
				errors.reject("appSettings.piqHost.id.invalid");
				return new ArrayList<>();
			}
			piqHosts.add(appSetting.getValue());
		}
		
		//deletePowerIQSettings(piqHosts);
		deletePowerIQSettingsUsingHostIds(piqHostIds);
		
		return piqHosts;
	}

	private void deletePowerIQSettingsUsingHostIds(List<Long> piqHostIds){
		Session session = sessionFactory.getCurrentSession();
		
		updateItemPIQInfoUsingIds(piqHostIds, session);
		
		updateLocationAppSettingsUsingIds(piqHostIds,session);
		
		String deleteHQL = "delete from ApplicationSetting where parentAppSettings.id in (:piqHostIds) or id in (:piqHostIds)";
		
		Query query = session.createQuery(deleteHQL);
		query.setParameterList("piqHostIds", piqHostIds);
		
		query.executeUpdate();
		
		
	}

	
	private ApplicationSetting addSettingInternal(long settingsLkpValueCode,
			String value, String attribute, ApplicationSetting parentApplicationSetting) {
		ApplicationSetting result = null;
		//First get the lksData out of the settingsLkpValueCode
		List<LksData> lksDataList = systemLookupDAO.findByLkpValueCode(settingsLkpValueCode);
		if (lksDataList.size() > 0){
			LksData lksData = lksDataList.get(0);
			result = new ApplicationSetting();
			result.setLksData(lksData);
			result.setValue(value);
			result.setAttribute(attribute);
			
			result.setParentAppSettings(parentApplicationSetting);
			
			result = addSetting(result);
		}
		return result;
	}


	
	
	private void deletePowerIQSettings(List<String> piqHosts, Session session) {
		String deleteHostQueryStr = "delete from ApplicationSetting where value in (:hosts)";
		Query deleteHostQuery = session.createQuery(deleteHostQueryStr);
		deleteHostQuery.setParameterList("hosts", piqHosts);
		deleteHostQuery.executeUpdate();
	}

	private void deletePowerIQAttributes(List<String> piqHosts, Session session) {
		String deleteAttributeQueryStr = "delete from ApplicationSetting as appSetting where appSetting.parentAppSettings in (from ApplicationSetting where value in (:hosts))";
		Query deleteAttributeQuery = session.createQuery(deleteAttributeQueryStr);
		deleteAttributeQuery.setParameterList("hosts", piqHosts);
		deleteAttributeQuery.executeUpdate();
	}

	private void updateLocationAppSettings(List<String> piqHosts,
			Session session) {		
		String updateAppSettingsOnLocationQryString = "update DataCenterLocationDetails set applicationSetting = null, piqId = null, piqExternalKey = null where applicationSetting in (from ApplicationSetting where value in (:hosts))";
		Query updateAppSettingsOnLocationQry = session.createQuery(updateAppSettingsOnLocationQryString);
		updateAppSettingsOnLocationQry.setParameterList("hosts", piqHosts);
		updateAppSettingsOnLocationQry.executeUpdate();
	}
	
	private void updateLocationAppSettingsUsingIds(List<Long> piqHostIds,
			Session session){		
		String updateAppSettingsOnLocationQryString = "update DataCenterLocationDetails set applicationSetting = null, piqId = null, piqExternalKey = null where applicationSetting in (from ApplicationSetting where id in (:hosts))";
		Query updateAppSettingsOnLocationQry = session.createQuery(updateAppSettingsOnLocationQryString);
		updateAppSettingsOnLocationQry.setParameterList("hosts", piqHostIds);
		updateAppSettingsOnLocationQry.executeUpdate();
	}
	
	//
	// Private helper methods
	//
	
	private ApplicationSetting addSetting(ApplicationSetting result) {
		Session session = sessionFactory.getCurrentSession();
		result = (ApplicationSetting) session.merge(result);
		return result;
	}
	
	private ApplicationSetting getParentApplicationSettings(String parentValue) {
		ApplicationSetting parentApplicationSetting = null;
		if (parentValue != null && !parentValue.isEmpty()){
			Session session = sessionFactory.getCurrentSession();
			Criteria c = session.createCriteria(ApplicationSetting.class);
			c.add(Restrictions.eq("value", parentValue));
			
			parentApplicationSetting = (ApplicationSetting) c.uniqueResult();
		}
		return parentApplicationSetting;
	}
	
	private ApplicationSetting getParentApplicationSettings(Long parentId) {
		ApplicationSetting parentApplicationSetting = null;
		if (parentId != null && parentId > 0){
			Session session = sessionFactory.getCurrentSession();
			parentApplicationSetting = (ApplicationSetting) session.get(ApplicationSetting.class, parentId);
		}
		return parentApplicationSetting;
	}
	
	private ApplicationSetting getAppSettingForName(Session session, Name name) {
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.createAlias("lksData", "lksData");
		c.add(Restrictions.eq("lksData.lkpValueCode", name.valueCode()));
		
		if (piqHostAppSettingId > 0 && name == Name.PIQ_IPADDRESS && powerIQHost != null && !powerIQHost.isEmpty()){
			c.add(Restrictions.eqOrIsNull("value", powerIQHost));
		} else	if (piqHostAppSettingId > 0 && name != Name.PIQ_IPADDRESS){
			c.createAlias("parentAppSettings", "parentAppSettings");
			c.add(Restrictions.eq("parentAppSettings.id",piqHostAppSettingId));
		}
		ApplicationSetting settings = null;

//		TODO: THE TRY/CATCH BLOCK MUST BE REMOVED AFTER ALL THE POWERIQ 
//		      RELATED FUNCTIONALITY GOES INTO THE POWERIQ SPECIFIC CONTEXT
		try {
			settings = (ApplicationSetting) c.uniqueResult();
		} catch (NonUniqueResultException nu){
			List list = c.list();
			settings = (ApplicationSetting) list.get(0);
		}
		
		return settings;
	}
	
	private String getAppSettingValueForName(Session session, Name name) {
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.createAlias("lksData", "lksData");
		c.add(Restrictions.eq("lksData.lkpValueCode", name.valueCode()));
		
		if (piqHostAppSettingId > 0 && name == Name.PIQ_IPADDRESS && powerIQHost != null && !powerIQHost.isEmpty()){
			c.add(Restrictions.eqOrIsNull("value", powerIQHost));
		} else 	if (piqHostAppSettingId > 0 && name != Name.PIQ_IPADDRESS){
			c.createAlias("parentAppSettings", "parentAppSettings");
			c.add(Restrictions.eq("parentAppSettings.id",piqHostAppSettingId));
		}
		
		c.setProjection((Projections.projectionList()
				.add(Projections.property("value"), "value")
				.add(Projections.property("lksData.lkpValue"), "lkpValue")));
		c.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		@SuppressWarnings("unchecked")
		Map<String, String> result = null;
		
//		TODO: THE TRY/CATCH BLOCK MUST BE REMOVED AFTER ALL THE POWERIQ 
//	      RELATED FUNCTIONALITY GOES INTO THE POWERIQ SPECIFIC CONTEXT		
		try {
			result = (Map<String,String>)c.uniqueResult();
		} catch (NonUniqueResultException nu){
			List list = c.list();
			result = (Map<String,String>)list.get(0);
		}
		if (result == null)
			return "";
		
		String value = result.get("value");
		if (value == null) value = result.get("lkpValue");
		return  value;
	}


	private ApplicationSetting getAppSettingUiFields(Session session, String uiField) {
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.createAlias("lksData", "lksData");
		c.add(Restrictions.eq("lksData.lkpTypeName", "UI_FIELDS"));
		c.add(Restrictions.eq("value", uiField));
		
		return (ApplicationSetting) c.uniqueResult();
	}	
	
	private Date getDate(String value) throws DataAccessException {
		Date date = null;
		
		if (value != null) {
			SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );
			try {
				date = sdf.parse( value );
			} 
			catch (ParseException e) {
				throw new DataAccessException(new ExceptionContext("Invalid date format", this.getClass(), e));
			}
		}
		
		return date;
	}
	
	private String formatDate(Date date) {
		String value = null;
		
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );
			value = sdf.format( date );
		}
		
		return value;
	}
	
	
	private long getPIQHostAppSettingId(){
		long hostAppSettingId = -1;
		
		if (powerIQHost != null){
			hostAppSettingId = getPIQHostAppSettingIdByHost(powerIQHost);
		}
		
		return hostAppSettingId;
	}

	private long getPIQHostAppSettingIdByHost(String piqHost) {
		if (piqHost == null || piqHost != null && piqHost.isEmpty()) return -1;
		Long hostAppSettingId = -1L;
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.add(Restrictions.eq("value", piqHost));
		c.add(Restrictions.isNull("parentAppSettings"));
		c.setProjection(Projections.id());
			
		hostAppSettingId = (Long) c.uniqueResult();
		
		if (hostAppSettingId == null) hostAppSettingId = -1L;
		
		return hostAppSettingId;
	}

	private long getPIQHostAppSettingIdByLabel(String label) {
		if (label == null || label != null && label.isEmpty()) return -1;
		Long hostAppSettingId = -1L;
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(ApplicationSetting.class);
		c.add(Restrictions.eq("value", label));

		c.createAlias("lksData", "lksData");
		c.createAlias("parentAppSettings", "parentAppSettings");
		c.add(Restrictions.eq("lksData.lkpTypeName", SystemLookup.ApplicationSettings.TypeName.PIQ_SETTING));
		
		c.add(Restrictions.isNotNull("parentAppSettings.id"));
		c.setProjection(Projections.property("parentAppSettings.id"));
			
		hostAppSettingId = (Long) c.uniqueResult();
		
		if (hostAppSettingId == null) hostAppSettingId = -1L;
		
		return hostAppSettingId;
	}


	private void updateItemPIQInfo(List<String> piqHosts, Session session) {
		String qry = "update Item set piqId = null, piqExternalKey = null where piqId is not null and dataCenterLocation in ( from DataCenterLocationDetails where applicationSetting in (from ApplicationSetting where value in (:hosts)))";
		Query query = session.createQuery(qry);
		query.setParameterList("hosts", piqHosts);
		query.executeUpdate();
	}

	private void updateItemPIQInfoUsingIds(List<Long> piqHosts, Session session) {
		String qry = "update Item set piqId = null, piqExternalKey = null where piqId is not null and dataCenterLocation in ( from DataCenterLocationDetails where applicationSetting in (from ApplicationSetting where id in (:hosts)))";
		Query query = session.createQuery(qry);
		query.setParameterList("hosts", piqHosts);
		query.executeUpdate();
	}


}
