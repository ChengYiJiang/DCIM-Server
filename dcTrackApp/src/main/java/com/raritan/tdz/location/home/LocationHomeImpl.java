package com.raritan.tdz.location.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.dctrack.xsd.UiView;
import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.ApplicationSetting;
import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.location.dao.LocationFinderDAO;
import com.raritan.tdz.location.dao.LocationUpdateDAO;
import com.raritan.tdz.location.validators.LocationValidator;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.piq.home.PIQLocationUnmap;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.settings.dao.ApplicationSettingsDAO;
import com.raritan.tdz.settings.home.ApplicationSettings;

public class LocationHomeImpl implements LocationHome {
	
	private static final String BUILDING = "Building";
	private static final String SITE = "Site";
	private static final String FLOOR = "Floor";
	
	protected Logger log = Logger.getLogger(this.getClass());

	@Autowired
	private PIQLocationUnmap piqLocationUnmap;
	private RulesProcessor rulesProcessor;
	
	private Map<Long, Long> hierarchyParentLocLookupMap = Collections.unmodifiableMap (new HashMap<Long, Long>(){{
		put(SystemLookup.DcLocation.ROOM,SystemLookup.DcLocation.FLOOR);
		put(SystemLookup.DcLocation.FLOOR,SystemLookup.DcLocation.BUILDING);
		put(SystemLookup.DcLocation.BUILDING,SystemLookup.DcLocation.SITE);
	}});
	
	private Map<Long, String> hierarchyParentLocNameMap = Collections.unmodifiableMap (new HashMap<Long, String>(){{
		put(SystemLookup.DcLocation.SITE,"Site");
		put(SystemLookup.DcLocation.BUILDING,"Building");
		put(SystemLookup.DcLocation.FLOOR,"Floor");
	}});
	
	@Autowired(required=true)
	private LocationDAO dao;

    @Autowired(required=true)
    private ItemDAO itemDao;

	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	protected RemoteRef remoteRef;
	protected ResourceBundleMessageSource messageSource;
	private LocationDomainAdaptor locationDomainAdaptor;
	@Autowired(required=true)
	private LocationValidator locaitonValidator;

	@Autowired(required=true)
	private LocationDTOAdapter locationDTOAdapter;	
	
	private List<String> validationWarningCodes;

	private ApplicationSettings appSettings;
	
	@Autowired
	private ApplicationSettingsDAO applicationSettingsDAO;

	@Autowired
	private LksCache lksCache;
	
	@Resource(name="validationInformationCodes")
	protected List<String> validationInformationCodes;

	
	public ApplicationSettings getAppSettings() {
		return appSettings;
	}

	public void setAppSettings(ApplicationSettings appSettings) {
		this.appSettings = appSettings;
	}

	public List<String> getValidationWarningCodes() {
		return validationWarningCodes;
	}

	public void setValidationWarningCodes(List<String> validationWarningCodes) {
		this.validationWarningCodes = validationWarningCodes;
	}

	public LocationDomainAdaptor getLocationDomainAdaptor() {
		return locationDomainAdaptor;
	}

	public void setLocationDomainAdaptor(LocationDomainAdaptor locationDomainAdaptor) {
		this.locationDomainAdaptor = locationDomainAdaptor;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}

	public LocationDAO getDao() {
		return dao;
	}

	public void setDao(LocationDAO dao) {
		this.dao = dao;
	}

	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}

	public LocationDTOAdapter getLocationDTOAdapter() {
		return locationDTOAdapter;
	}

	public void setLocationDTOAdapter(LocationDTOAdapter locationDTOAdapter) {
		this.locationDTOAdapter = locationDTOAdapter;
	}

	private boolean locationExists(Long locationId){
		boolean retval = true;
		if (null == dao.getLocation(locationId)) retval = false;
		return retval;
	}
	
	private boolean isUserVisiableLocation(Long locationId){
		boolean retval = true;
		LocationFinderDAO finderDAO = (LocationFinderDAO) dao;
		List<String> codesList = finderDAO.findLocationCodeById(locationId);
		if( codesList != null && codesList.size() > 0 ){
			String code = codesList.get(0); 
			if( code.equals(BUILDING) || code.equals(SITE) || code.equals(FLOOR)){
				retval = false;
			}
		}
		
		return retval;
	}
	
	//code defined in dcTrackApp/src/main/resources/locale/exceptions_en_US.properties
	private void throwBusinessValidationExc( String code, Object[] args, Locale locale) throws BusinessValidationException{
		String msg = messageSource.getMessage(code, args, locale);
		BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
		ex.addValidationError(msg);
		ex.addValidationError(code, msg);
		throw ex;
	}

	@Override
	@Transactional (readOnly = true)
	public Map<String, UiComponentDTO> getLocationDetails(Long locationId, UserInfo userInfo)
			throws Throwable {

		if( ! isUserVisiableLocation(locationId) ){
			throwBusinessValidationExc("locationValidator.invalidLocation", null, null);
		}
		
		Map<String, UiComponentDTO> dtos = new HashMap<String, UiComponentDTO>();
		@SuppressWarnings("deprecation")

		String unit = (userInfo.getUnits() != null) ? userInfo.getUnits() : "1";
		try{
			List<UiComponentDTO> componentList = new ArrayList<>();
			
			UiView uiView = rulesProcessor.getData("uiView[@uiId='locationView']/", "dataCenterLocationId", locationId, "=", new LongType(), unit);
			JXPathContext jc = JXPathContext.newContext(uiView);
			componentList = jc.selectNodes("uiViewPanel/uiViewComponents/uiViewComponent");
			
			//FIXME: Throw exception if there is no requested location in the DB!!!

			for (UiComponentDTO uiViewComponent : componentList) {
				dtos.put(uiViewComponent.getUiId(), uiViewComponent);
				UnitConverterIntf unitConverter = remoteRef.getRemoteRefUnitConverter(uiViewComponent.getUiValueIdField().getRemoteRef());
				Object in_val = uiViewComponent.getUiValueIdField().getValue();
				if (null != unitConverter && null != in_val) {
					Object value = unitConverter.convert(in_val, unit);
					uiViewComponent.getUiValueIdField().setValue(value);
				}
				dtos.put(uiViewComponent.getUiId(), uiViewComponent);
				//TODO: For web GUI handle fill out info if field is required or not by reading FieldDetails
			}//for
			
		}catch(Exception e){
			throwBusinessValidationExc("locationValidator.invalidLocation", null, null);
		}
		return dtos;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Map<String, Object> getLocationDetailsExt(Long locationId, UserInfo userInfo)
			throws Throwable {
		
		Map<String, UiComponentDTO> locationUiDto = getLocationDetails(locationId, userInfo);
			
		Map<String, Object> retval = locationDTOAdapter.convertLocationDetails( locationUiDto );
		
		return retval;
	}

	@Override
	@Transactional
	public Map<String, UiComponentDTO> saveLocation(Long locationId, List<ValueIdDTO> dtoList, UserInfo sessionUser) 
			throws ClassNotFoundException, BusinessValidationException, Throwable {
		
		return saveLocation(locationId, dtoList, null, sessionUser);
	}

	@Transactional
	private Map<String, UiComponentDTO> saveLocation(Long locationId, List<ValueIdDTO> dtoList, 
			Map<String, Object> locationDetails, UserInfo sessionUser) 
			throws ClassNotFoundException, BusinessValidationException, Throwable {
		
		if( isUserVisiableLocation(locationId) == false ){
			throwBusinessValidationExc("locationValidator.invalidLocation", null, null);
		}
		
		boolean update = locationId > 0 ? true : false;
		
		//Create or get the location details
		DataCenterLocationDetails location = createOrGetLocation(locationId, SystemLookup.DcLocation.ROOM);
		
		//Convert the dtoList to the actual DataCenterLocationDetails.	
		locationDomainAdaptor.convert(location, dtoList, ((sessionUser != null) ? sessionUser.getUnits(): "1"));
		
		updatePIQHost(location, locationDetails);
		
		//Validate Location details
		validate(location, sessionUser);
		
		//Setup default site
		setDefaultSite(location);
		
		//Save if everything okay
		if (update) {
			dao.merge(location);
		} else {
			locationId = dao.createLocation(location);
		}
		
		updateVPCSettings(locationId, locationDetails, update);
		
		log.debug("[ saveLocation ] locationId: " + locationId);
		log.debug("[ saveLocation.updateVPCSettings ] locationId: " + locationId);
		
		//Return the location details
		return getLocationDetails(locationId, sessionUser);
	}
	
	@Override
	@Transactional
	public Map<String, Object> saveLocationExtAPI(long locationId, Map<String, Object> locationDetails, UserInfo user) 
			throws BusinessValidationException, ClassNotFoundException, Throwable {
		
		Map<String, Object> retval = null;
		
		log.debug("[ saveLocationExtAPI ] locationId : " + locationId);
		log.debug("[ saveLocationExtAPI ] locationDetails : " + locationDetails);
		log.debug("[ saveLocationExtAPI ] UserInfo : " + user);
		
		List<ValueIdDTO> dtoList = locationDTOAdapter.convertToDTOList(locationId, locationDetails);
		Map<String, UiComponentDTO> locationUiDto = saveLocation(locationId, dtoList, locationDetails, user);
		
		if( locationUiDto != null ) retval = locationDTOAdapter.convertLocationDetails( locationUiDto );		
		
		return retval;
	}
	
	/**
	 * NOTE: We have separate call for update (do not reuse save) because  when user provides 
	 * locationId > 0 in attempt to modify location, but locationId does not exist in the DB, we have to
	 * thrown an exception.
	 */
	@Override
	@Transactional
	public Map<String, Object> updateLocationExtAPI(long locationId, Map<String, Object> locationDetails, UserInfo user) 
			throws BusinessValidationException, ClassNotFoundException, Throwable {

		Map<String, Object> retval = null;
		
		log.debug("[ updateLocationExtAPI ] locationId : " + locationId);
		log.debug("[ updateLocationExtAPI ] locationDetails : " + locationDetails);
		log.debug("[ updateLocationExtAPI ] UserInfo : " + user);
		
		if (locationId>0 && locationExists(locationId)) {
			List<ValueIdDTO> dtoList = locationDTOAdapter.convertToDTOList(locationId, locationDetails);
			Map<String, UiComponentDTO> locationUiDto = saveLocation(locationId, dtoList, locationDetails, user);
			
			if ( locationUiDto != null ) retval = locationDTOAdapter.convertLocationDetails( locationUiDto );
		} else {
			log.debug("[ updateLocationExtAPI ] throwBusinessValidationExc...");
			throwBusinessValidationExc( "LocationConvert.cannotUpdateLocation", null, null); 
		}
		
		log.debug("[ updateLocationExtAPI ] retval : " + retval);
		
		return retval;
	}
	
	
	// --------------------- private methods go here -----------------------
	
	private void updateVPCSettings(long locationId, Map<String, Object> locationDetails, boolean update) {
		
		if (locationId < 0 || locationDetails == null || !locationDetails.containsKey("enableVPC")) return;
		
		try {
			boolean enableVPC = ((Boolean)locationDetails.get("enableVPC")).booleanValue();
			if (!update && !enableVPC) return;

			Long appSettingLkpValueCode = SystemLookup.ApplicationSettings.VPC_SETTINGS.ENABLED;
			ApplicationSetting appSetting = applicationSettingsDAO.getAppSetting(appSettingLkpValueCode, locationId);
			
			String enableVPCStr = enableVPC ? "true" : "false";
			
			if (null == appSetting) {
				appSetting = new ApplicationSetting();
				appSetting.setLksData(lksCache.getLksDataUsingLkpCode(appSettingLkpValueCode));
				appSetting.setLocationId(locationId);
				appSetting.setValue(enableVPCStr);
				applicationSettingsDAO.create(appSetting);
			} else {
				appSetting.setValue(enableVPCStr);
				applicationSettingsDAO.update(appSetting);
			}
		} catch (Throwable ex) {
		}
	}

	private void updatePIQHost(DataCenterLocationDetails location, Map<String, Object> locationDetails) 
			throws BusinessValidationException, DataAccessException {
		
		if (location == null || locationDetails == null || !locationDetails.containsKey("piqMappingName")) {
			return;
		}	

		List<ApplicationSetting> piqSettings = null;
		
		piqSettings = getPowerIQSettings(locationDetails, "piqApplianceName");
		
		if (piqSettings == null || piqSettings.size() == 0) {
			piqSettings = getPowerIQSettings(locationDetails, "ipAddress or hostname");
		}
		
		setupPowerIQSettings(location, piqSettings);
	}
	
	private List<ApplicationSetting> getPowerIQSettings(Map<String, Object> locationDetails, String fieldName) 
			throws BusinessValidationException, DataAccessException {
		
		List<ApplicationSetting> piqSettings = null;
		
		Object fieldValue = locationDetails.get("piqMappingName");	
		log.debug("[ updatePIQHost ] piqMappingName: " + fieldValue);
		
		String fieldValueStr = fieldValue != null ? fieldValue.toString().trim() : "";
		try {
			if (!fieldValueStr.isEmpty()) {
				if ("piqApplianceName".equals(fieldName)) {
					piqSettings = appSettings.getSettingsByPIQLabel(fieldValueStr);
				} else {
					piqSettings = appSettings.getSettingsByPIQHost(fieldValueStr);
					if (piqSettings == null || piqSettings.size() == 0) {
						throw new Exception();
					}	
				}
			}
		} catch (Exception ex) {
			Object args[]  = {fieldValueStr};
			throwBusinessValidationExc("appSettings.invalidPiqSettings", args, null);
		}
		
		return piqSettings;
	}
	
	private void setupPowerIQSettings(DataCenterLocationDetails location, List<ApplicationSetting> piqSettings) {
			
		if (piqSettings == null) {
			location.setApplicationSetting(null);
			return;
		}
		
		for (ApplicationSetting applicationSetting: piqSettings) {		
			
			long piqIpAddressLkpValueCode = SystemLookup.ApplicationSettings.PIQ_IPADDRESS;
			long appSettingLkpValueCode = applicationSetting.getLksData().getLkpValueCode();

			if (piqIpAddressLkpValueCode == appSettingLkpValueCode) {
				location.setApplicationSetting(applicationSetting);
				break;
			}
		}	
	}
	
	private DataCenterLocationDetails createOrGetLocation(Long locationId, Long hierarchyLkpValueCode)
			throws BusinessValidationException {
		DataCenterLocationDetails location = null;
		if (locationId > 0){
			LocationFinderDAO finderDAO = (LocationFinderDAO) dao;
			List<DataCenterLocationDetails> locationList = finderDAO.fetchById(locationId);
			if (locationList != null && locationList.size() == 1)
				location = locationList.get(0);
			else {
				throwBusinessValidationExc("LocationConvert.cannotUpdateLocation", null, null); 
			}
		} else {
			//If not create a new location
			location = new DataCenterLocationDetails();
			location.setDcLocaleDetails(new DataCenterLocaleDetails());
			location.setComponentTypeLookup(systemLookupFinderDAO.findByLkpValueCode(hierarchyLkpValueCode).get(0));
			
			//Check to see if we have the parent location
			//NOTE that at this moment we will have a static hierarchy for SITE, BULDING, FLOOR, ROOM[...]
			DataCenterLocationDetails parentLoc = getParentLocation(hierarchyParentLocLookupMap.get(hierarchyLkpValueCode));
			if (parentLoc != null){
				//Set the proper parent location
				location.setParentLocation(parentLoc);
			} else {
				hierarchyLkpValueCode = hierarchyParentLocLookupMap.get(hierarchyLkpValueCode);
				if (hierarchyLkpValueCode != null){
					//Parent is not available, then create one
					DataCenterLocationDetails parentLocation = createOrGetLocation(locationId,hierarchyLkpValueCode);
					//Setup its name, code and current location's parent
					if (parentLocation != null){
						parentLocation.setCode(hierarchyParentLocNameMap.get(hierarchyLkpValueCode));
						parentLocation.setDcName(hierarchyParentLocNameMap.get(hierarchyLkpValueCode));
						location.setParentLocation(parentLocation);
					}
				}
			}
		}
		return location;
	}
	
	private DataCenterLocationDetails getParentLocation(Long hierarchyLkpValueCode){
		if (hierarchyLkpValueCode == null)
			return null;
		
		LocationFinderDAO finderDao = (LocationFinderDAO)dao;
		List<DataCenterLocationDetails> parentLocList = finderDao.findLocationsByHierarchy(hierarchyLkpValueCode);
		if (parentLocList.size() == 1){
			return parentLocList.get(0);
		} else {
			return null;
		}
	}
	
	private void validate(DataCenterLocationDetails location, UserInfo sessionUser)
			throws BusinessValidationException {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, location.getClass().getName());
		
		//Validate user
		validateUserPermission(sessionUser, errors);
		
		if (!errors.hasErrors()){
			//Add validation errors from the domain adaptor.
			errors.addAllErrors(locationDomainAdaptor.getValidationErrors());
		}
		
		//Make sure that user has permission to proceed. If not throw the request right away
		//There is a reason why we have two if checks against errors. 
		//The reason is that if there are errors in the domain adaptor, we do not want to proceed
		//with the validating any other things since the error messages will be incorrect anyway.
		if (!errors.hasErrors()){
			//Validate the rest
			locaitonValidator.validate(location, errors);
		}
		
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationWarningCodes != null && validationWarningCodes.contains(error.getCode())){
					e.setCallbackURL("itemService.saveItem");
					e.addValidationWarning(msg);
					e.addValidationWarning(error.getCode(), msg);
				} else {
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg);
				}
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0){
			throw e;
		}
	}

	private void validateUserPermission(UserInfo sessionUser, Errors errors) {
		if (sessionUser == null){
			errors.reject("locationValidator.noPermission");
			return;
		}
		
		if (!sessionUser.isAdmin()){
			errors.reject("locationValidator.noPermission");
			return;
		}
	}
	
	private void setDefaultSite(DataCenterLocationDetails location) throws DataAccessException {
		Long locationId = location.getDataCenterLocationId() == null ? -1 : location.getDataCenterLocationId();
		LocationUpdateDAO updateDao = (LocationUpdateDAO)dao;
		//Update all the other sites default value to false except the one that is getting updated.
		//Please refer to LocationsDAOFunctions.xml for the query which does the updates.
		updateDao.updateDefaultSiteExcludeCurrent(false, locationId);
	}
	
	@Override
	@Transactional
	public List<Map<String, UiComponentDTO>>getAllLocationsDetails(UserInfo userInfo) throws Throwable{
		List<Map<String, UiComponentDTO>> retval = new ArrayList<Map<String, UiComponentDTO>>();
		 
		LocationFinderDAO finderDAO = (LocationFinderDAO) dao;
		List<Long> locationsIds = finderDAO.findAllVisibleLocationsId();
		if( locationsIds != null && locationsIds.size() > 0){
			for( Long locationId : locationsIds){
				Map<String, UiComponentDTO> locationDetails = getLocationDetails(locationId, userInfo);
				retval.add(locationDetails);
			}
		}
		return retval;
	}
	
	@Override
	@Transactional
	public Object getAllLocationsDetailsExt( UserInfo userInfo) throws Throwable {
		Map<String, Object> retval = new LinkedHashMap<String, Object>();			
		List<Map<String, Object>> locArray = new ArrayList<Map<String, Object>>();
			
		List<Map<String, UiComponentDTO>> locationUiDTOList = getAllLocationsDetails( userInfo);
		for( Map<String, UiComponentDTO> locationUiDto : locationUiDTOList){
			Map<String, Object> convertedLocation = locationDTOAdapter.convertLocationDetails( locationUiDto );
			locArray.add(convertedLocation);
		}
		
		String key = "locations";
		retval.put(key, locArray);

		return retval;	
	}

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void deleteLocationExtAPI(long locationId, UserInfo user)
            throws BusinessValidationException, ClassNotFoundException,
            Throwable {

		log.debug("[ deleteLocationExtAPI ] locationId : " + locationId);
		log.debug("[ deleteLocationExtAPI ] UserInfo : " + user);

        //Do　business validation and check user permission...
        validateDelete(locationId, user);

        dao.deleteLocationAndItems(locationId, user.getUserName());
    }

    /**
     * Validate if the site can be deleted.
     * 
     * @param location
     * @param userInfo
     * @param numOfNumPlannedItems
     * @throws Throwable
     */
    private void validateDelete(long locationId, UserInfo userInfo) throws Throwable {
        // create an errors object
        Map<String, String> errorMap = new HashMap<String, String>();
        MapBindingResult errors = new MapBindingResult(errorMap, DataCenterLocationDetails.class.getName());

        // validate if the user has permission
        validateUserPermission(userInfo, errors);

        // validate if there is no non-planned items		
        long numOfNonPlannedItems = itemDao.getNumOfNonPlannedItems(locationId);       
		log.debug("[ validateDelete ] numOfNonPlannedItems : " + numOfNonPlannedItems);
		
        if (numOfNonPlannedItems > 0) {
            errors.reject("locationValidator.nonPlannedItems");
        }

        //If there are no errors from previous step, validate if this location has any items that are not in planned state. If there are then you should fill up the errors pointer with that message
        //If there are errors, then throw a BusinessValidationException
        BusinessValidationException.throwBusinessValidationException(errors, null, validationInformationCodes, messageSource, this.getClass(), null);
    }

	@Override
	public void unmapLocationExtAPI(Long locationId, UserInfo userInfo)
			throws BusinessValidationException {

		Errors errors = new MapBindingResult( new HashMap<String, String>(), DataCenterLocaleDetails.class.getName() );

		piqLocationUnmap.unmap(locationId, userInfo, errors);
		
	}
}