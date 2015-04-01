/**
 * 
 */
package com.raritan.tdz.piq.home;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.RemoteDataAccessException;

/**
 * @author prasanna
 *
 */
public interface PIQSyncLocationClient extends PIQRestClient {
	
	public static final String LOC_TYPE_DC = "DataCenter";
	public static final String LOC_TYPE_FLOOR = "Floor";
	public static final String LOC_TYPE_ROOM = "Room";
	
	
	/**
	 * Add a Location from dcTrack database to PowerIQ database
	 * @param DataCenterLocationDetails - location
	 * @return String containing powerIqId
	 */
	public String addDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException;
	
	/**
	 * Update Location details from dcTrack database to PowerIQ database
	 * @param location
	 * @throws RemoteDataAccessException
	 */
	public void updateDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException;

	/**
	 * Deletes a data center from PIQ.
	 * @param dataCenterId the ID of the data center in PIQ
	 * @return true if location was successfully deleted
	 */
	public void deleteDataCenter(String dataCenterId)
		throws RemoteDataAccessException;
	
	/**
	 * Checks to see if given Location (ID) exists in PIQ
	 * @param String - powerIQ ID
	 */
	public boolean isLocationInSync(String piq_id)
		throws RemoteDataAccessException;
	
	/**
	 * Checks to see if given Location exists and is in sync
	 * @param DataCenterLocationDetails - location
	 */
	public boolean isLocationInSync(DataCenterLocationDetails location)
		throws RemoteDataAccessException;
	
	/**
	 * Get the parent id
	 * @param id
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public String getParent(String id) throws RemoteDataAccessException;

	/**
	 * Map the datacenter with PowerIQ site using location's external key in the 
	 * @param location
	 * @return
	 * @throws RemoteDataAccessException
	 */
	String mapByExternalKey(DataCenterLocationDetails location)
			throws RemoteDataAccessException;
}

