package com.raritan.tdz.piq.home;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.RemoteDataAccessException;

public interface PIQSyncSiteClient<T> extends PIQRestClient {
	
	/**
	 * Add a Location from dcTrack database to PowerIQ database
	 * @param DataCenterLocationDetails - location
	 * @return String containing powerIqId
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public String addDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException, InstantiationException, IllegalAccessException;
	
	/**
	 * Update Location details from dcTrack database to PowerIQ database
	 * @param location
	 * @throws RemoteDataAccessException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void updateDataCenter(DataCenterLocationDetails location)
			throws RemoteDataAccessException, InstantiationException, IllegalAccessException;

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


}
