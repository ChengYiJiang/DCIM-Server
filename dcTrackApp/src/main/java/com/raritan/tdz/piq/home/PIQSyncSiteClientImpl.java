/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.http.ResponseEntity;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.DataCenterJSON;
import com.raritan.tdz.piq.json.DataCenterJSON.DataCenter;
import com.raritan.tdz.piq.json.ErrorJSON;
import com.raritan.tdz.piq.json.SitesJSON;
import com.raritan.tdz.port.home.InvalidPortObjectException;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQSyncSiteClientImpl<T> extends PIQSyncBase implements
		PIQSyncSiteClient<T>{
	
	private String piqIdPrefix;
	
	protected Class<T> type;

	public PIQSyncSiteClientImpl(ApplicationSettings appSettings,
			String piqIdPrefix) throws DataAccessException {
		super(appSettings);
		this.piqIdPrefix = piqIdPrefix;
	}

	public String getPiqIdPrefix() {
		return piqIdPrefix;
	}

	public void setPiqIdPrefix(String piqIdPrefix) {
		this.piqIdPrefix = piqIdPrefix;
	}

	public PIQSyncSiteClientImpl(ApplicationSettings appSettings)
			throws DataAccessException {
		super(appSettings);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncLocationClient#addLocation(com.raritan.tdz.domain.DataCenterLocationDetails)
	 */
	@Override
	public String addDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException, InstantiationException, IllegalAccessException {
		
		String piq_id = null;
		
		T dcjson = type.newInstance();
		
		try {
			// if already exists update data from piq rather than pushing.
			SitesJSON response = (SitesJSON) doRestPostRequest(dcjson, type);
			
			if (isAppSettingsEnabled() && response != null && response.getId() != null)
				piq_id =  piqIdPrefix + response.getId() ;
			else
				piq_id = null;
		} catch (RemoteDataAccessException e) {
			ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
			// Handle External Key Exists.
			if (isPIQExternalKeyExist( error )) {
				piq_id = handleExternalKeyExist(location,
						(String) getValue(dcjson, "externalKey"), false);
			} else {
				throw e;
			}
		}
		
		return piq_id;
	}

	private Object getValue(Object port, String methodName) {
		Object value = null;
		try {
			value = PropertyUtils.getProperty(port, methodName);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for " + methodName + ": Internal Error");
		}
		return value;
	}

	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncLocationClient#isLocationInSync(java.lang.String)
	 */
	@Override
	public boolean isLocationInSync(String dataCenterId)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()) {

			SitesJSON dcjson = null;
			ResponseEntity<?> restResult = null;
			
			if (dataCenterId != null && (new Integer(dataCenterId)).intValue() > 0){
				restResult = doRestGet(dataCenterId, type);
			}
			
			if (restResult != null){
				dcjson = (SitesJSON) restResult.getBody();
				
				if (dcjson != null && dcjson.getId() != null 
						&& dcjson.getId().equals(dataCenterId)) 
					result = true;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("isLocationInSync: " + result);
		}
		return result;
	}

	@Override
	public void deleteDataCenter(String siteId)
			throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (siteId != null)) {
			String id = PIQSyncLocationUtil.getActualId(siteId);
			doRestDelete( id );
			if (log.isDebugEnabled()) {
				log.debug("deleteLocation: Deleting PIQ data center with id=" + siteId);
			}
		}
		
	}

	private String findDCId(String externalKey) throws RemoteDataAccessException {
		String dcId = null;
		
		ResponseEntity<?> resp = doRestGet("?external_key_eq=" + externalKey, type);
		
		if (resp != null) {
			SitesJSON json = (SitesJSON)resp.getBody();
			if (json != null) {
				
/*				List<DataCenter> dcs = json.getDataCenters();
				if (dcs != null && !dcs.isEmpty()) {
					dcId = dcs.get(0).getId();
				}*/
				
				dcId = json.getSiteId();
			}
		}
		
		return dcId;
	}

	private String handleExternalKeyExist(DataCenterLocationDetails dcLocation, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		//Find the power IQ Id 
		String id = findDCId(externalKey);
		
		//Set it to rackItem
		dcLocation.setPiqId(piqIdPrefix + id);
		
		//Call update to make sure everything is in sync.
		//updateRack(rackItem, skipSyncCheck);
		
		return piqIdPrefix + id;
	}

	@Override
	public boolean isLocationInSync(DataCenterLocationDetails location)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			SitesJSON dcjson = null;
			ResponseEntity<?> restResult = null;
			
			String locationId = location.getPiqId();
			if (locationId != null){
				String dataCenterId = PIQSyncLocationUtil.getActualId(locationId);
				if (dataCenterId != null && (new Integer(dataCenterId)).intValue() > 0){
					restResult = doRestGet(dataCenterId, DataCenterJSON.class);
				}
				
				if (restResult != null){
					dcjson = (DataCenterJSON) restResult.getBody();
					
					if (dcjson != null && dcjson.isDataCenterInSync(location)) 
						result = true;
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("isLocationInSync: " + result);
		}
		return result;
	}

	@Override
	public void updateDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException, InstantiationException, IllegalAccessException {

		T dcjson = type.newInstance();
		try {
			String piqId = PIQSyncLocationUtil.getActualId (location.getPiqId());
			if (piqId != null && !piqId.equals("")) {
				doRestPut(dcjson, piqId);
			}
		} catch (RemoteDataAccessException e){
			ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
			// Handle External Key Exists.
			throw e;
		}
		
	}
}

