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
import com.raritan.tdz.piq.json.RoomJSON;
import com.raritan.tdz.piq.json.RoomJSON.Room;
import com.raritan.tdz.piq.json.SiteParentJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQSyncRoomClientImpl extends PIQSyncBase implements
		PIQSyncDCClient {

	private String piqIdPrefix = "Room:";
	
	@Autowired
	private PIQSyncLocationUtil piqSyncLocationUtil;

	public PIQSyncRoomClientImpl(ApplicationSettings appSettings)
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

		RoomJSON dcjson = new RoomJSON(location);
		try {
			// add datacenter
			RoomJSON response = (RoomJSON) doRestPostRequest(dcjson, RoomJSON.class);
			if (response != null && response.getRoom() != null)
				piqId =  piqIdPrefix + response.getRoom().getId();
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
	public boolean isLocationInSync(String roomId)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			RoomJSON roomjson = null;
			ResponseEntity<?> restResult = null;
			
			if (roomId != null && (new Integer(roomId)).intValue() > 0){
				restResult = doRestGet(roomId, RoomJSON.class);
			}
			
			if (restResult != null){
				roomjson = (RoomJSON) restResult.getBody();
				
				if (roomjson != null && roomjson.getRoom() != null
						&& roomjson.getRoom().getId().equals(roomId)) 
					result = true;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("isLocationInSync: " + result);
		}
		return result;
	}

	@Override
	public void deleteDataCenter(String roomId)
			throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (roomId != null)) {
			if (log.isDebugEnabled()) {
				log.debug("deleteLocation: Deleting PIQ room with id=" + roomId);
			}
			String piqId = PIQSyncLocationUtil.getActualId (roomId);
			doRestDelete( piqId );
		}
		
	}


	@Override
	public boolean isLocationInSync(DataCenterLocationDetails location)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			RoomJSON roomjson = null;
			ResponseEntity<?> restResult = null;
			
			String locationId = location.getPiqId();
			if (locationId != null){
				String roomId = PIQSyncLocationUtil.getActualId(locationId);
				if (roomId != null && (new Integer(roomId)).intValue() > 0){
					restResult = doRestGet(roomId, RoomJSON.class);
				}
				
				if (restResult != null){
					roomjson = (RoomJSON) restResult.getBody();
					if (roomjson != null && roomjson.getRoom() != null
							&& roomjson.getRoom().isRoomInSync(location)) 
						result = true;
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("isLocationInSync(ROOM): " + result);
		}
		return result;
	}

	@Override
	public void updateDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException {
		RoomJSON dcjson = new RoomJSON(location);
		String piqId = PIQSyncLocationUtil.getActualId (location.getPiqId());
		if (piqId != null && !piqId.equals("")) {
			doRestPut(dcjson, piqId);
		}
		location.setPiqExternalKey(dcjson.getExternalComplexKey());
	}
	
	private String findRoomId(String externalKey) throws RemoteDataAccessException {
		String roomId = null;
		ResponseEntity<?> resp = doRestGet("?external_key_eq=" + externalKey, RoomJSON.class);
		
		if (resp != null) {
			RoomJSON json = (RoomJSON)resp.getBody();
			if (json != null) {
				List<Room> rooms = json.getRooms();
				if (rooms != null && !rooms.isEmpty()) {
					roomId = rooms.get(0).getId();
				}
			}
		}
		return roomId;
	}


	private String handleExternalKeyExist(DataCenterLocationDetails dcLocation, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		String piqId = null;
		
		if (externalKey == null || externalKey.isEmpty()) return null;

		String extKey = PIQSyncLocationUtil.getActualId(dcLocation.getPiqExternalKey());

		//Find the power IQ Id based on external key 
		String id = findRoomId(extKey);
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
		return "";
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
