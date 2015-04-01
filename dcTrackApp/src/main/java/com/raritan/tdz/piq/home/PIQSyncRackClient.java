package com.raritan.tdz.piq.home;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;

/**
 * @author prasanna
 * This interface provides all the methods required to sync a dcTrack Item to PIQ 
 * by calling PIQ rest calls.
 */
public interface PIQSyncRackClient extends PIQRestClient {

	/**
	 * Add a IT Item from dcTrack database to PowerIQ database
	 * @param skipSyncCheck TODO
	 * @param Item - PIQ Rack Item - Cabinet item
	 * @return String - PIQ ID
	 * @throws RemoteDataAccessException 
	 */
	public String addRack(Item rackItem, boolean skipSyncCheck) throws RemoteDataAccessException;
	
	/**
	 * Checks to see if given Rack (ID) exists in PIQ
	 * @param piqRackId rack ID 
	 * @throws RemoteDataAccessException 
	 */
	public boolean isRackInSync(String piq_id) throws RemoteDataAccessException;
	
	/**
	 * Checks to see if given Rack exists in PIQ and is in Sync.
	 * Note that unlike the method that takes in PowerIQ ID, 
	 * this checks more than just powerIQ Id. It checks all
	 * the parameters in JSON received from power IQ to 
	 * what is in dcTrack database.
	 * @param piqRackId rack ID 
	 * @throws RemoteDataAccessException 
	 */
	public boolean isRackInSync(Item item) throws RemoteDataAccessException;
	
	/**
	 * Updates the rack
	 * @param skipSyncCheck TODO
	 * @param Item - PIQ Rack Item - Cabinet item
	 * @return String - PIQ ID
	 * @throws RemoteDataAccessException 
	 */
	public String updateRack(Item rackItem, boolean skipSyncCheck) throws RemoteDataAccessException;

	/**
	 * Deletes a Rack Item from PIQ.
	 * @param rackItem
	 * @throws RemoteDataAccessException
	 */
	public void deleteRack(String piqRackId) throws RemoteDataAccessException;

	/**
	 * This will check if all the racks are in sync.
	 * @param PIQItem list of items that needs to be checked against PIQ
	 * @throws RemoteDataAccessException 
	 */
	public void areRacksInSync(List<PIQItem> piqItems) throws RemoteDataAccessException;
	
	/**
	 * Get the items not in sync
	 * @param deviceId the ID of the device in PIQ that we are trying to move
	 * @param rackId the ID of the rack in PIQ to move the device
	 */
	public PIQItemNotInSync getPIQItemsInSync();
	
	/**
	 * Update the external key for the rack item
	 * @param rackItem rack item
	 * @param piqIdForReset TODO
	 * @param reset Should we reset to the original PIQ generated key?
	 * @return
	 * @throws RemoteDataAccessException 
	 */
	
	public String updateExternalKey(Item rackItem, String piqIdForReset, boolean reset) throws RemoteDataAccessException;
	
	/**
	 * Get parent PowerIQ Id based on rack
	 * @param rackItem
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public String getParentPIQId(Item rackItem) throws RemoteDataAccessException;
	
	/**
	 * 
	 * @param rackItem
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public String mapParent(Item rackItem)	throws RemoteDataAccessException;
}
