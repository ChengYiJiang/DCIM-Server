package com.raritan.tdz.settings.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.ApplicationSetting;
import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.dto.PIQBulkSyncStatusDTO;
import com.raritan.tdz.piq.home.PIQSettingsHome;
import com.raritan.tdz.piq.integration.PowerIQRouter;
import com.raritan.tdz.piq.service.PIQBulkSyncService;
import com.raritan.tdz.settings.dto.ApplicationSettingDTO;
import com.raritan.tdz.settings.dto.PiqSettingDTO;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.validators.PIQSettingsDTOValidator;
import com.raritan.tdz.snmp.home.SnmpSettingsHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.vpc.home.VPCHome;

/**
 * Application Settings Service implementation.
 * @author Andrew Cohen
 */
public class ApplicationSettingServiceImpl implements ApplicationSettingsService {
	
	private final Logger log = Logger.getLogger( ApplicationSettingsService.class );
	
	private ApplicationSettings settingsHome;
	
	private PIQSettingsHome piqSettings;
	
	private SnmpSettingsHome snmpSettings;
	
	private PIQBulkSyncService piqBulkSyncService;
	
	private PowerIQRouter powerIQRouter;
	
	//private PIQSyncPIQVersion piqInfoService;
	private PIQInfoService piqInfoService;
	
	@Autowired
	private LocationDAO locationDAO;
	
	@Autowired
	private VPCHome vpcHome;
	
	@Autowired
	private PIQSettingsDTOValidator piqSettingsDTOValidator;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private Validator vpcCircuitExist;
	
	public ApplicationSettingServiceImpl(ApplicationSettings settingsHome) {
		this.settingsHome = settingsHome;
	}
	
	public void setPiqInfoService(PIQInfoService piqInfoService){				
		this.piqInfoService = piqInfoService;
	}
			
	public void setPiqSettings(PIQSettingsHome piqSettings) {		
		this.piqSettings = piqSettings;
	}
	
	public void setSnmpSettings(SnmpSettingsHome snmpSettings) {		
		this.snmpSettings = snmpSettings;
	}

	public void setPiqBulkSyncService(PIQBulkSyncService piqBulkSyncService) {		
		this.piqBulkSyncService = piqBulkSyncService;
	}

	
	public PowerIQRouter getPowerIQRouter() {
		return powerIQRouter;
	}

	public void setPowerIQRouter(PowerIQRouter powerIQRouter) {
		this.powerIQRouter = powerIQRouter;
	}

	@Override
	public List<ApplicationSettingDTO> getApplicationSettings() throws ServiceLayerException {
		return getSettingsInternal( null );
	}

	@Override
	public List<ApplicationSettingDTO> getApplicationSettings(String locationCode) throws ServiceLayerException {
		return getSettingsInternal( locationCode );
	}

	@Override
	public List<ApplicationSettingDTO> getPIQSettings() throws ServiceLayerException {
		return getSettingsByType( SystemLookup.ApplicationSettings.TypeName.PIQ_SETTING );
	}
	
	@Override
	public List<ApplicationSettingDTO> getPIQSettings(String piqHost)
			throws ServiceLayerException {
		if (piqHost != null && !piqHost.isEmpty())
			return getSettingsByPIQHost(piqHost);
		else
			return getSettingsByType( SystemLookup.ApplicationSettings.TypeName.PIQ_SETTING );
	}

	@Override
	public PIQBulkSyncStatusDTO updatePIQData(String piqHost) throws ServiceLayerException {
		
		if (piqHost == null || piqHost != null && piqHost.isEmpty()){
			throw new BusinessValidationException(new ExceptionContext("Power IQ Host is null", this.getClass()));
		}
		
		List<ApplicationSetting> settingsList = settingsHome.getSettingsByPIQHost(piqHost);
		String userName = "";
		String password = "";
		
		for (ApplicationSetting setting: settingsList){
			if(setting.getLksData().getLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_USERNAME) userName = setting.getValue();
			else if(setting.getLksData().getLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_PASSWORD) password = setting.getValue();
		}
		
		ApplicationCodesEnum testStatus = piqSettings.testSettings(piqHost,
				userName,
				password
		);
		
		if (testStatus.errCode() != ApplicationCodesEnum.PIQ_SETTINGS_VALIDATED.errCode()) {
			throw new BusinessValidationException(new ExceptionContext(testStatus, this.getClass(), new Exception()));
		}
		
		//String ipAddr = piqHost == null ? settingsHome.getProperty(Name.PIQ_IPADDRESS) : piqHost;
		
		return piqBulkSyncService.updatePIQData(piqHost);
		
	}
	
	@Override
	public PIQBulkSyncStatusDTO getPIQUpdateDataStatus(String piqHost) throws ServiceLayerException  {		
		//String ipAddr = settingsHome.getProperty(Name.PIQ_IPADDRESS);
		if (piqHost != null && !piqHost.isEmpty() && piqBulkSyncService != null){
			PIQBulkSyncStatusDTO dto = piqBulkSyncService.getPIQUpdateDataStatus(piqHost);
			dto.setPiqHost(piqHost);
			return dto;
		}else
			return new PIQBulkSyncStatusDTO();
	}
	
	@Override
	public int updatePIQSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException {
		PIQAppSettingResult result = updatePIQApplicationSettings( appSettings );
		int count = 0;
		if (result != null){
			count = result.getCount();
			
			if (result.getPiqHostsToDelete() != null){
				//Cleanup the contexts
				cleanPowerIQContexts(result.getPiqHostsToDelete());
			}
			
			if (result.getPiqHosts() != null){
				for (String piqHost:result.getPiqHosts()){
					if (piqHost != null && !piqHost.isEmpty()){
						PIQBulkSyncStatusDTO bulkSyncStatus = getPIQUpdateDataStatus(piqHost);
						if (!bulkSyncStatus.isJobRunning())
							piqSettings.reloadSettings( piqHost );
					}
				}
			}
		}
		return count;
	}
	
	@Override
	public PIQBulkSyncStatusDTO stopPIQDataUpdate(String piqHost) throws ServiceLayerException {
		
		//String ipAddr = settingsHome.getProperty(Name.PIQ_IPADDRESS);
		if (piqHost != null && !piqHost.isEmpty())
			return piqBulkSyncService.stopPIQDataUpdate(piqHost);
		else
			return new PIQBulkSyncStatusDTO();
	}
	


	@Override
	public Boolean isPIQUpdatedRunning() throws ServiceLayerException {
		Boolean result = false;
		//Get all the powerIQ hosts
		List<String> piqHostsInDB = settingsHome.getAllPowerIQHosts();
		
		//For each one of them, see if for any one is running a powerIQ update by gettting the status
		for (String piqHost:piqHostsInDB){
			if (piqHost != null && !piqHost.isEmpty()){
				PIQBulkSyncStatusDTO dto = getPIQUpdateDataStatus(piqHost);
				if (dto.isJobRunning()){
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public int testPIQSettings(String piqHost, String username, String password) throws ServiceLayerException {
		return piqSettings.testSettings(piqHost, username, password).errCode();
	}

	/** CR56619 Populate PIQ Version */
	@Override
	public PiqSettingDTO testPIQSettingsAndGetVersion(PiqSettingDTO piqSettingDTO) throws ServiceLayerException {								
		
		try {
			
			int errorCode = piqSettings.testSettings(piqSettingDTO.getIpAddress(), piqSettingDTO.getUserName(), piqSettingDTO.getPassword()).errCode();
						
			piqSettingDTO.setResultCode( String.valueOf(errorCode) );
			
			if(Integer.parseInt(piqSettingDTO.getResultCode()) == ApplicationCodesEnum.PIQ_SETTINGS_VALIDATED.errCode()){			
				piqSettingDTO  = piqInfoService.getPIQVersion(piqSettingDTO);
				if(Boolean.parseBoolean(piqSettingDTO.getUnsaved()) == false){
					settingsHome.updateSetting(Long.parseLong(piqSettingDTO.getVersionAppSid()), piqSettingDTO.getVersion());
				}
			}
			//compare and save
			//piqSettingDTO.setVersion(version);
						
		} catch (RemoteDataAccessException e) {		
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		return piqSettingDTO;
	}
	
	@Override
	public List<ApplicationSettingDTO> getSNMPSettings() throws ServiceLayerException {
		return getSettingsByType( SystemLookup.ApplicationSettings.TypeName.SNMP_SETTING );
	}
	
	@Override
	public int updateSNMPSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException {
		int count = updateApplicationSettings(appSettings);
		snmpSettings.configureSNMP( settingsHome );
		return count;
	}

	@Override
	public List<ApplicationSettingDTO> getVPCSettings() throws ServiceLayerException {
		return getSettingsByType( SystemLookup.ApplicationSettings.TypeName.VPC_SETTING );
	}

	private Errors checkVPCCircuitExist(List<ApplicationSettingDTO> appSettings) {

		Errors errors = getErrorObject();
		
		for (ApplicationSettingDTO dto: appSettings) {
			
			if (dto.getAppValue().equals("true")) continue;
			
			Map<String, Object> targetMap = new HashMap<String, Object>();
			targetMap.put(DataCenterLocaleDetails.class.getName(), dto.getLocationId());
		
			vpcCircuitExist.validate(targetMap, errors);
		}
		
		return errors;
	}
	
	@Override
	public int updateVPCSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException {

		int count = 0;
		
		Errors errors = checkVPCCircuitExist(appSettings);
		if (errors.hasErrors()) {
			
			throwBusinessValidationException(errors);
		}
		else {
		
			count = updateApplicationSettings(appSettings);
		}
		
		return count;
	}

	@Override
	public int testSNMPSettings(boolean v1v2Enabled, boolean v3Enabled,
			String readCommunityString, String username,
			String password) throws ServiceLayerException {
		return snmpSettings.testSNMPSettings(v1v2Enabled, v3Enabled, readCommunityString, username, password);
	}
	
	@Override
	public void testBusinessException() throws ServiceLayerException {
		BusinessValidationException e = new BusinessValidationException(new ExceptionContext("User entered incorrect data", this.getClass()));
		e.addValidationError("This is the first validation error");
		e.addValidationError("This is the second validation error");
		e.addValidationError("This is the third validation error");
		throw e;
	}
	
	@Override
	public void testSystemException() throws ServiceLayerException {
		throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), new Exception()));
	}
	
	
	//
	// Private methods
	//

	private List<ApplicationSettingDTO> getSettingsByType(String lkpTypeName) throws ServiceLayerException {
		List<ApplicationSettingDTO> settings = new LinkedList<ApplicationSettingDTO>();
		
		for (ApplicationSetting setting : settingsHome.getSettingsByType( lkpTypeName )) {
			settings.add( new ApplicationSettingDTO(setting, locationDAO) );
		}
		
		return settings;
	}
	
	private List<ApplicationSettingDTO> getSettingsByPIQHost(String piqHost) throws ServiceLayerException {
		List<ApplicationSettingDTO> settings = new LinkedList<ApplicationSettingDTO>();
		
		for (ApplicationSetting setting : settingsHome.getSettingsByPIQHost( piqHost )) {
			if (setting != null)
				settings.add( new ApplicationSettingDTO(setting, locationDAO) );
		}
		
		return settings;
	}
	
	private List<ApplicationSettingDTO> getSettingsInternal(String locationCode) throws DataAccessException {
		List<ApplicationSettingDTO> ret = new LinkedList<ApplicationSettingDTO>();
		Collection<ApplicationSetting> settings =  settingsHome.getSettings( locationCode );
		
		for (ApplicationSetting setting : settings) {
			ret.add( new ApplicationSettingDTO( setting, locationDAO ) );
		}
		
		return ret;
	}
	
	private int updateApplicationSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException {
		int count = 0;
		for (ApplicationSettingDTO appSetting : appSettings) {
			try {
				settingsHome.updateSetting(appSetting.getAppSettingId(), appSetting.getAppValue());
				count++;
			}
			catch (DataAccessException e) {
				log.error("Failed to update application setting : " + appSetting, e );
			}
		}
		return count;
	}
	
	
	private PIQAppSettingResult updatePIQApplicationSettings(List<ApplicationSettingDTO> appSettings) throws ServiceLayerException {
		int count = 0;
		long parentAppSettingId = -1;
		PIQAppSettingResult result =  new PIQAppSettingResult();
		
		
		if (appSettings != null){
			
			Errors errors = getErrorObject();
			
			piqSettingsDTOValidator.validate(appSettings, errors);
			
			if (errors.hasErrors()){
				throwBusinessValidationException(errors);
			}
			
			List<String> piqHostsProcessed = new ArrayList<>();
			
			Map<String,ApplicationSettingDTO> newPIQSettingsMap = new HashMap<String,ApplicationSettingDTO>();
			Set<String> parentGroupingIds = new LinkedHashSet<String>();
			
			//List<String> piqHostCloseContextsOnUpdate = new ArrayList<>();
			
			for (ApplicationSettingDTO appSetting : appSettings) {
				try {
					ApplicationSetting setting = null;
					if (appSetting.getAppSettingId() == -1 && appSetting.getParentAppSettingId() == -1){
						newPIQSettingsMap.put(appSetting.getParentGroupingId() + appSetting.getSettingLkpValueCode(), appSetting);
						parentGroupingIds.add(appSetting.getParentGroupingId());
					} else if (appSetting.getAppSettingId() == -1 && appSetting.getParentAppSettingId() > 0){
						settingsHome.addSetting(appSetting.getSettingLkpValueCode(),  appSetting.getAppValue(), null, appSetting.getParentAppSettingId());
					} else {
						
						
						String oldPiqHost = "";
						String newPiqHost = "";
						if (appSetting.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_IPADDRESS){
							oldPiqHost = settingsHome.getProperty(appSetting.getAppSettingId());
						}
						
						setting = settingsHome.updateSetting(appSetting.getAppSettingId(), appSetting.getAppValue(), appSetting.getParentAppSettingId());
						
						if (appSetting.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_IPADDRESS){
							piqHostsProcessed.add(appSetting.getAppValue());
							newPiqHost = appSetting.getAppValue();
						}
						
						//If the user is modifying the host then we need to make sure that we close the old piq host spring integration context
						if (newPiqHost != null && !newPiqHost.equals(oldPiqHost)){
							if (result.getPiqHostsToDelete() == null) result.setPiqHostsToDelete(new ArrayList<String>());
							
							result.getPiqHostsToDelete().add(oldPiqHost);
						}
					}
					
					if (setting != null && setting.getLksData().getLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_IPADDRESS){
					//	parentAppSettingId = setting.getId();
						result.addPiqHost(setting.getValue());
					}
					count++;
				}
				catch (DataAccessException e) {
					log.error("Failed to update application setting : " + appSetting, e );
				}
			}
			
//			if (piqHostCloseContextsOnUpdate.size() > 0){
//				cleanPowerIQContexts(piqHostCloseContextsOnUpdate);
//			}
			
			//If there are any new ones, we need to add them all
			addPIQApplicationSettings(parentGroupingIds,newPIQSettingsMap, result);
			
			result.setCount(count);
		}
		
		return result;
	}

	private void addPIQApplicationSettings(Set<String> parentGroupingIds, Map<String,ApplicationSettingDTO> newPIQSettingsMap, PIQAppSettingResult result) throws ServiceLayerException{
		
		//First add the PIQHost records and collect them in a map
		Map<String,ApplicationSetting> powerIQHostIdMap = new HashMap<String,ApplicationSetting>();
		
		for (Map.Entry<String, ApplicationSettingDTO> entry:newPIQSettingsMap.entrySet()){
			ApplicationSettingDTO appSetting = entry.getValue();
			String parentGroupId = entry.getKey();
			if (appSetting.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_IPADDRESS){
				ApplicationSetting setting = settingsHome.addSetting(appSetting.getSettingLkpValueCode(), appSetting.getAppValue(), null, -1L);
				powerIQHostIdMap.put(parentGroupId.replaceAll(new Long(appSetting.getSettingLkpValueCode()).toString(), ""), setting);
				result.addPiqHost(setting.getValue());
			}
		}
		
		//Now add all the other row elements to the database per PowerIQ
		for (Map.Entry<String, ApplicationSettingDTO> entry:newPIQSettingsMap.entrySet()){
			ApplicationSettingDTO appSetting = entry.getValue();
			String parentGroupId = entry.getKey();
			if (appSetting.getSettingLkpValueCode() != SystemLookup.ApplicationSettings.PIQ_IPADDRESS){
				settingsHome.addSetting(appSetting.getSettingLkpValueCode(), appSetting.getAppValue(), null, powerIQHostIdMap.get(parentGroupId.replaceAll(new Long(appSetting.getSettingLkpValueCode()).toString(), "")).getId());
			}
		}
		
		//Add the additional attributes not sent by the client per PowerIQ
		for (String parentGroupingId:parentGroupingIds){
			settingsHome.addSetting(SystemLookup.ApplicationSettings.PIQ_POLLING_INTERVAL, "3", "minutes", powerIQHostIdMap.get(parentGroupingId).getId());
			settingsHome.addSetting(SystemLookup.ApplicationSettings.PIQ_EVENT_QUERY_DATE, "", "PIQ event query date", powerIQHostIdMap.get(parentGroupingId).getId());
			settingsHome.addSetting(SystemLookup.ApplicationSettings.PIQ_VERSION, "", null,powerIQHostIdMap.get(parentGroupingId).getId());
		}
	}
	
	@Override
	public int updateRequiredUiFields(List<ValueIdDTO> appSettings) throws ServiceLayerException {
		
		for(ValueIdDTO rec:appSettings){
			settingsHome.updateUiFields(rec.getLabel(), "required=" + String.valueOf(rec.getData()));
		}
		
		return 0;
	}
	
	@Override
	public Map<String, UiComponentDTO> getUiFieldsInitState() throws ServiceLayerException {
		Map<String, UiComponentDTO> recList = new HashMap<String, UiComponentDTO>();
		
		//This code assume that first "field=value" pair is for the required field. 
		for(ApplicationSetting rec:settingsHome.getSettingsByType("UI_FIELDS")){
			UiComponentDTO dto = new UiComponentDTO();
			String temp[] = rec.getAttribute().split("=");
			
			if(temp.length > 1 && temp[1] != null){
				dto.setRequired(temp[1].equals("true"));
				dto.setUiId(rec.getValue());
				recList.put(rec.getValue(), dto);
			}
		}
		
		return recList;
	}	
	

	@Override
	public List<ApplicationSettingDTO> deletePIQSettings(List<String> piqHosts)
			throws ServiceLayerException {
		settingsHome.deletePowerIQSettings(piqHosts);
		
		cleanPowerIQContexts(piqHosts);
		return getPIQSettings();
	}


	


	@Override
	public List<ApplicationSettingDTO> deletePIQSettingsUsingHostIds(
			List<Integer> piqHostIntIds) throws ServiceLayerException {

        List<Long> piqHostIds = prepareLongListCriteria(piqHostIntIds);

		Errors errors = getErrorObject();
		List<String> piqHosts = settingsHome.deletePowerIQSettings(piqHostIds, errors);
		if (errors.hasErrors()){
			throwBusinessValidationException(errors);
		}
		
		cleanPowerIQContexts(piqHosts);
		return getPIQSettings();
	}


	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, ApplicationSettingsService.class.getName());
		return errors;
	}
	
	//Protected methods
	protected void throwBusinessValidationException(Errors errors) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
  				{
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg);
				}
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		}
	}

	private void cleanPowerIQContexts(List<String> piqHosts)
			throws DataAccessException {
		//Cleanup the contexts
		for (String piqHost:piqHosts){
			if (powerIQRouter != null)
				powerIQRouter.remove(piqHost);
		}
	}
	
	private class PIQAppSettingResult {
		private List<String> piqHosts = new ArrayList<String>();
		private int count;
		
		private List<String> piqHostsToDelete;
		
		public List<String> getPiqHosts() {
			return piqHosts;
		}
		
		public void addPiqHost(String piqHost) {
			this.piqHosts.add(piqHost);
		}
		
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public List<String> getPiqHostsToDelete() {
			return piqHostsToDelete;
		}
		public void setPiqHostsToDelete(List<String> piqHostsToDelete) {
			this.piqHostsToDelete = piqHostsToDelete;
		}
		
		
	}


    private List<Long> prepareLongListCriteria(List<Integer> values) {
        if (values == null) return null;
        List<Long> list = new ArrayList<Long>( values.size() );
        for (Integer i : values) {
            list.add( i.longValue() );
        }
        return list;
    }

	@Override
	public boolean isVPCEnabledForLocation(String locationCode) throws ServiceLayerException {
		for(ApplicationSettingDTO ap:getSettingsByType( SystemLookup.ApplicationSettings.TypeName.VPC_SETTING )) {
			if(ap.getLocationCode().equalsIgnoreCase(locationCode) && ap.getAppValue() != null) {
				if(ap.getAppValue().isEmpty()) continue;
				
				return Boolean.parseBoolean(ap.getAppValue());
			}
		}
		
		return false;
	}


}
