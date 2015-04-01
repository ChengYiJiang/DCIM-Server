package com.raritan.tdz.piq.home;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.RemoteDataAccessException;

public interface PIQSyncDCClient extends PIQRestClient {
	
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
	 * @return TODO
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
	 * @param type : request type - dc, floor, room
	 * @param id - PowerIQ ID
	 * @return
	 * @throws RemoteDataAccessException 
	 */
	public String getParent (String type, String id) throws RemoteDataAccessException;

	/**
	 * Map dcTrack location to a PowerIQ site using external key
	 * @param location
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public String mapByExternalKey(DataCenterLocationDetails location)
			throws RemoteDataAccessException;
}
