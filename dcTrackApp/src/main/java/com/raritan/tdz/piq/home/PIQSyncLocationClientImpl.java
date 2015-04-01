/**
 * 
 */
package com.raritan.tdz.piq.home;


import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQSyncLocationClientImpl extends PIQSyncBase implements
		PIQSyncLocationClient {

	private final String defaultLocationType = "DataCenter";

	private static final Map<String, String> locMap = 
		    Collections.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;
			{ 
		        put("DATA_CENTER", "DataCenter");
		        put("FLOOR", "Floor");
		        put("ROOM", "Room");
		    }});
	
	private LinkedHashMap<String,PIQSyncDCClient> dcClients = null;
	
	public PIQSyncLocationClientImpl(ApplicationSettings appSettings,
			 LinkedHashMap<String, PIQSyncDCClient> dcClients)
			throws DataAccessException {
		super(appSettings);
		this.dcClients = dcClients;
	}
	
	@Override
	public String mapByExternalKey(DataCenterLocationDetails location) throws RemoteDataAccessException {
		String piqId = null;
		PIQSyncDCClient dcClient = null;
		if (location != null && location.getPiqExternalKey() != null) {
			String locationType = getLocationTypeByExternalKey (location.getPiqExternalKey());
			dcClient = dcClients.get(locationType);
			if (dcClient != null) {
				piqId =  dcClient.mapByExternalKey(location);
				if (piqId != null) {
					// prefix with parent for leaf nodes (floors or rooms) 
					piqId = getParent (piqId);
				}
			}
		}
		return piqId;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncLocationClient#AddLocation(com.raritan.tdz.domain.DataCenterLocationDetails)
	 */
	@Override
	public String addDataCenter(DataCenterLocationDetails location) throws RemoteDataAccessException {
		PIQSyncDCClient dcClient = null; 
		String piqId = null;
		
		if (isAppSettingsEnabled() && location != null) {
			if (location.getPiqExternalKey() != null) {
				piqId = mapByExternalKey(location);
			}
			else {
				 // Default location for adding site in dcTrack is DataCenter
				dcClient = dcClients.get(defaultLocationType);
				if (dcClient != null) {
					piqId =  dcClient.addDataCenter(location);
					// no need to resolve parent for the root.
					// if (piqId != null) {
					//	// prefix with parent for leaf nodes (floors or rooms) 
					//	piqId = getParent (piqId);
					// }
				}
			}
		} 
		return piqId;
	}
	
	@Override
	public String getParent(String id) throws RemoteDataAccessException {
		String newId = null;
		if (id != null && !id.isEmpty()) {
			String type = PIQSyncLocationUtil.getLocationType(id);
			PIQSyncDCClient dcClient = dcClients.get(type);
			if (dcClient != null) { 
				newId = dcClient.getParent(type, id);
				if (newId != null) {
					if (newId.equals("root")) {
						return id;
					} else {
						newId = getParent(newId);
					}
				}
			}
		}
		if (newId != null) {
			newId = (newId + ":" + id);
		}
		return newId;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncLocationClient#isLocationInSync(java.lang.String)
	 */
	@Override
	public boolean isLocationInSync(String locationId) throws RemoteDataAccessException{
		boolean result = false;
		
		if (isAppSettingsEnabled() && locationId != null) {
			String locationType = PIQSyncLocationUtil.getLocationType(locationId);
			PIQSyncDCClient client = dcClients.get(locationType);
			if (client != null) {
				String id = PIQSyncLocationUtil.getActualId(locationId);
				result = client.isLocationInSync(id);
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("No location client found for " + locationType);
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncLocationClient#deleteLocation(com.raritan.tdz.domain.DataCenterLocationDetails)
	 */
	@Override
	public void deleteDataCenter(String dataCenterId) throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (dataCenterId != null)) {
			if (log.isDebugEnabled()) {
				log.debug("deleteLocation: Deleting PIQ data center with id=" + dataCenterId);
			}
			String locationType = getLocationTypeByPiqId (dataCenterId);
			PIQSyncDCClient dcClient = dcClients.get(locationType);
			if (dcClient != null) {
				dcClient.deleteDataCenter(dataCenterId);
			}
		}
	}

	@Override
	public boolean isLocationInSync(DataCenterLocationDetails location)
			throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled() && location != null ){
			String locationId = location.getPiqId();
			if (locationId != null){
				String id = PIQSyncLocationUtil.getLocationType(locationId);
				PIQSyncDCClient client = dcClients.get(id);
				if (client != null) {
					result = client.isLocationInSync(location);
				}
				else {
					if (log.isDebugEnabled()) {
						log.debug("No location client found for " + id);
					}
				}
			}
		}
		return result;
	}

	@Override
	public void updateDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && location != null) {
			if (log.isDebugEnabled()) {
				log.debug("updateLocation: updating PIQ data center with name = " + location.getDcName());
			}
			String locationType = null;
			if (location.getPiqId() != null && !location.getPiqId().isEmpty()) {
				// from piqId identify the location type 
				locationType = getLocationTypeByPiqId (location.getPiqId());
			}
			PIQSyncDCClient dcClient = dcClients.get(locationType);
			if (dcClient != null) {
				dcClient.updateDataCenter(location);  
			}
		}
	}
	
	private String getLocationTypeByExternalKey (String piqExternalKey) {
		// external key consists of Object:key 
		// e.g. Object = ROOM, FLOOR, DATACENTER
		// e.g. key = "Room -- i", "Floor -- 1", DataCenter-- 1"
		String type = PIQSyncLocationUtil.getLocationType(piqExternalKey);
		return locMap.get(type);
	}

	private String getLocationTypeByPiqId (String dataCenterId) {
		// The piqId for site may consists of 
		// For DataCenter -  DC:<piqId>
		// For Floor - DC:<piqId>:Floor:<piqId>
		// For Room -  DC:<piqId>:Floor:<piqId>:Room:<piq_id>
		return PIQSyncLocationUtil.getLocationType(dataCenterId);
	}

}
