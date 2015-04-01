/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.DataCenterJSON;
import com.raritan.tdz.piq.json.DataCenterJSON.DataCenter;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQSyncDCClientImpl extends PIQSyncBase implements
		PIQSyncDCClient {
	
	private String piqIdPrefix = "DataCenter:";
	
	@Autowired
	private PIQSyncLocationUtil piqSyncLocationUtil;

	public PIQSyncDCClientImpl(ApplicationSettings appSettings)
			throws DataAccessException {
		super(appSettings);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncLocationClient#addLocation(com.raritan.tdz.domain.DataCenterLocationDetails)
	 */
	@Override
	public String addDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException{
		
		String piqId = null;
		
		if (!isAppSettingsEnabled()) return piqId; 
		
		DataCenterJSON dcjson = new DataCenterJSON(location);
		try {
			// add datacenter
			DataCenterJSON response = (DataCenterJSON) doRestPostRequest(dcjson, DataCenterJSON.class);
			
			if (response != null && response.getDataCenter() != null)
				piqId =  piqIdPrefix + response.getDataCenter().getId();
			else
				piqId = null;
		} catch (RemoteDataAccessException e){
			if (dcjson.getExternalKey() != null) {
				// map datacenter by external key
				piqId = handleExternalKeyExist(location, location.getPiqExternalKey(), false);
			} else {
				throw e;
			}
		}
		return piqId;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncLocationClient#isLocationInSync(java.lang.String)
	 */
	@Override
	public boolean isLocationInSync(String dataCenterId)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			DataCenterJSON dcjson = null;
			ResponseEntity<?> restResult = null;
			if (dataCenterId != null && (new Integer(dataCenterId)).intValue() > 0){
				restResult = doRestGet(dataCenterId, DataCenterJSON.class);
			}
			
			if (restResult != null){
				dcjson = (DataCenterJSON) restResult.getBody();
				if (dcjson != null && dcjson.getDataCenter() != null 
						&& dcjson.getDataCenter().getId().equals(dataCenterId)) 
					result = true;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("isLocationInSync: " + result);
		}
		return result;
	}

	@Override
	public void deleteDataCenter(String dataCenterId)
			throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (dataCenterId != null)) {
			String id = PIQSyncLocationUtil.getActualId(dataCenterId);
			doRestDelete( id );
			if (log.isDebugEnabled()) {
				log.debug("deleteLocation: Deleting PIQ data center with id=" + dataCenterId);
			}
		}
	}

	private String findDCId(String externalKey) throws RemoteDataAccessException {
		String dcId = null;
		ResponseEntity<?> resp = doRestGet("?external_key_eq=" + externalKey, DataCenterJSON.class);
		if (resp != null) {
			DataCenterJSON json = (DataCenterJSON)resp.getBody();
			if (json != null) {
				List<DataCenter> dcs = json.getDataCenters();
				if (dcs != null && !dcs.isEmpty()) {
					dcId = dcs.get(0).getId();
				}
			}
		}
		return dcId;
	}

	private String handleExternalKeyExist(DataCenterLocationDetails dcLocation, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		String piqId = null;

		if (externalKey == null || externalKey.isEmpty()) return null;

		String extKey = PIQSyncLocationUtil.getActualExtkey(externalKey);
		//Find the power IQ Id 
		String id = findDCId(extKey);
		if (id != null) {
			piqId = piqIdPrefix + id;
			dcLocation.setPiqId(piqId);
		}
		else {
			try { 
				// set event
				piqSyncLocationUtil.setLocationEvent(externalKey, EventType.CANNOT_ACCESS_RESOURCE, EventSeverity.CRITICAL, eventSource);
			}catch (DataAccessException e) {
				// exception is consumed here as done for all piq integration related code
				if (log.isDebugEnabled())
					e.printStackTrace();
			}
		}
		return piqId;
	}

	@Override
	public boolean isLocationInSync(DataCenterLocationDetails location)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			DataCenterJSON dcjson = null;
			ResponseEntity<?> restResult = null;
			
			String locationId = location.getPiqId();
			if (locationId != null){
				String dataCenterId = PIQSyncLocationUtil.getActualId(locationId);
				if (dataCenterId != null && (new Integer(dataCenterId)).intValue() > 0){
					restResult = doRestGet(dataCenterId, DataCenterJSON.class);
				}
				
				if (restResult != null){
					dcjson = (DataCenterJSON) restResult.getBody();
					
					if (dcjson != null && dcjson.getDataCenter() != null 
							&& dcjson.getDataCenter().isDataCenterInSync(location)) 
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
			throws RemoteDataAccessException {

		DataCenterJSON dcjson = new DataCenterJSON(location);
		String piqId = PIQSyncLocationUtil.getActualId (location.getPiqId());
		if (piqId != null && !piqId.isEmpty()) {
			doRestPut(dcjson, piqId);
			//piqId = piqIdPrefix + piqId;
			// TODO:
		}
		location.setPiqExternalKey(dcjson.getExternalComplexKey());
	}

	@Override
	public String getParent(String type, String id) {
		return "root";
	}

	@Override
	public String mapByExternalKey(DataCenterLocationDetails location)
			throws RemoteDataAccessException{
		String piqId = null;
		if (isAppSettingsEnabled()) { 
			piqId = handleExternalKeyExist(location, location.getPiqExternalKey(), false);
		}
		return piqId;
	}
}

