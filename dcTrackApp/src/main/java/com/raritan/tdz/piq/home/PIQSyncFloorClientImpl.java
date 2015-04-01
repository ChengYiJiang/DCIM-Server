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
import com.raritan.tdz.piq.json.FloorJSON;
import com.raritan.tdz.piq.json.FloorJSON.Floor;
import com.raritan.tdz.piq.json.SiteParentJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQSyncFloorClientImpl extends PIQSyncBase implements
		PIQSyncDCClient {

	private String piqIdPrefix = "Floor:";
	
	@Autowired
	private PIQSyncLocationUtil piqSyncLocationUtil;
	
	public PIQSyncFloorClientImpl(ApplicationSettings appSettings)
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

		FloorJSON dcjson = new FloorJSON(location);
		try {
			// add datacenter
			FloorJSON response = (FloorJSON) doRestPostRequest(dcjson, FloorJSON.class);
			if (response != null && response.getFloor() != null)
				piqId =  piqIdPrefix + response.getFloor().getId();
			else
				piqId = null;
		} catch (RemoteDataAccessException e){
			// map datacenter by external key
			if (dcjson.getExternalKey() != null) {
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
	public boolean isLocationInSync(String floorId)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			FloorJSON floorjson = null;
			ResponseEntity<?> restResult = null;
			if (floorId != null && (new Integer(floorId)).intValue() > 0){
				restResult = doRestGet(floorId, FloorJSON.class);
			}
			
			if (restResult != null){
				floorjson = (FloorJSON) restResult.getBody();
				
				if (floorjson != null && floorjson.getFloor() != null 
						&& floorjson.getFloor().getId().equals(floorId)) 
					result = true;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("isLocationInSync: " + result);
		}
		return result;
	}

	@Override
	public void deleteDataCenter(String floorId)
			throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (floorId != null)) {
			if (log.isDebugEnabled()) {
				log.debug("deleteLocation: Deleting PIQ floor with id=" + floorId);
			}
			String id = PIQSyncLocationUtil.getActualId(floorId);
			doRestDelete( id );
		}
		
	}

	@Override
	public boolean isLocationInSync(DataCenterLocationDetails location)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			FloorJSON floorjson = null;
			ResponseEntity<?> restResult = null;
			
			String locationId = location.getPiqId();
			if (locationId != null){
				String floorId = PIQSyncLocationUtil.getActualId(locationId);
				if (floorId != null && (new Integer(floorId)).intValue() > 0){
					restResult = doRestGet(floorId, FloorJSON.class);
				}
				
				if (restResult != null){
					floorjson = (FloorJSON) restResult.getBody();
					
					if (floorjson != null && floorjson.getFloor() != null 
							&& floorjson.getFloor().isFloorInSync(location)) 
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
		FloorJSON dcjson = new FloorJSON(location);
		String piqId = PIQSyncLocationUtil.getActualId (location.getPiqId());
		if (piqId != null && !piqId.isEmpty()) {
			doRestPut(dcjson, piqId);
		}
		location.setPiqExternalKey(dcjson.getExternalComplexKey());
	}

	private String handleExternalKeyExist(DataCenterLocationDetails dcLocation, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		String piqId = null;
		
		if (externalKey == null || externalKey.isEmpty()) return null;
		
		String extKey = PIQSyncLocationUtil.getActualId(dcLocation.getPiqExternalKey());
		
		//Find the power IQ Id based on external key 
		String id = findFloorId(extKey);
		if (id != null) {
			piqId = piqIdPrefix + id;
			dcLocation.setPiqId(piqId);
		}
		else {
			try {
				// set event
				piqSyncLocationUtil.setLocationEvent(externalKey, EventType.CANNOT_ACCESS_RESOURCE, EventSeverity.CRITICAL, eventSource);
			} catch (DataAccessException e) {
				// exception is consumed here as done for all piq integration related code
				if (log.isDebugEnabled())
					e.printStackTrace();
			}
		}
		return piqId;
	}
	
	private String findFloorId(String externalKey) throws RemoteDataAccessException {
		String floorId = null;
		ResponseEntity<?> resp = doRestGet("?external_key_eq=" + externalKey, FloorJSON.class);
		if (resp != null) {
			FloorJSON json = (FloorJSON)resp.getBody();
			if (json != null) {
				//JB Fix this.
				List<Floor> Floors = json.getFloors();
				if (Floors != null && !Floors.isEmpty()) {
					floorId = Floors.get(0).getId();
				}
			}
		}
		return floorId;
	}

	@Override
	public String getParent(String type, String id) throws RemoteDataAccessException {
		String qstr = PIQSyncLocationUtil.getActualId(id) + "/parent";
		ResponseEntity<?> resp = doRestGet(qstr, SiteParentJSON.class);
		if (resp != null) {
			SiteParentJSON json = (SiteParentJSON)resp.getBody();
			if (json != null) {
				return json.getId();
			}
		}
		return null;
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
