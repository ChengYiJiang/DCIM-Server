package com.raritan.tdz.piq.home;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.DeviceJSON;

/**
 * @author prasanna
 * This interface provides all the methods required to sync a dcTrack Item to PIQ 
 * by calling PIQ rest calls.
 */
public interface PIQSyncDeviceClient extends PIQRestClient {
	
	/**
	 * Add a Device from dcTrack database to PowerIQ database
	 * @param ipAddress IPAddress of the device
	 * @param powerRating Power Rating for the power supply of that device
	 * @param skipSyncCheck Do you want to skip checking if device is in sync?
	 * @param Item - PIQ IT Item - device, network, probe, cabinet
	 * @return 
	 * @throws RemoteDataAccessException 
	 */
	public String addDevice(Item itItem, String ipAddress, Integer powerRating, boolean skipSyncCheck) throws RemoteDataAccessException;
	
	
	/**
	 * Update a Device from dcTrack database to PowerIQ database
	 * @param ipAddress IPAddress of the device
	 * @param powerRating Power Rating for the power supply of that device
	 * @param skipSyncCheck Do you want to skip checking if device is in sync?
	 * @param Item - PIQ IT Item - device, network, probe, cabinet
	 * @return 
	 * @throws RemoteDataAccessException 
	 */
	public String updateDevice(Item itItem, String ipAddress, Integer powerRating, boolean skipSyncCheck) throws RemoteDataAccessException;
	
	/**
	 * Checks to see if given Device (ID) exists in PIQ
	 * @param String - powerIQ ID
	 * @throws RemoteDataAccessException 
	 */
	public boolean isDeviceInSync(String deviceId) throws RemoteDataAccessException;
	
	/**
	 * Return the device information for a given device in PIQ.
	 * @param deviceId the device ID.
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public DeviceJSON getDevice(String deviceId) throws RemoteDataAccessException;
	
	/**
	 * Checks to see if given Device exists in PIQ and is in Sync.
	 * Note that unlike the method that takes in PowerIQ ID, 
	 * this checks more than just powerIQ Id. It checks all
	 * the parameters in JSON received from power IQ to 
	 * what is in dcTrack database.
	 * @param ipAddress TODO
	 * @param powerRating TODO
	 * @param String - powerIQ ID
	 * @throws RemoteDataAccessException 
	 */
	public int isDeviceInSync(Item device, String ipAddress, Integer powerRating) throws RemoteDataAccessException;
	
	/**
	 * Delete a device from PowerIQ.
	 * @param deviceId the ID of the device in PIQ
	 */
	public void deleteDevice(String deviceId) throws RemoteDataAccessException;
	
	/**
	 * Move a device to a different Rack.
	 * @param itItem the item that we are trying to move
	 * @param rackId the ID of the rack in PIQ to move the device
	 */
	public void moveDeviceTo(Item itItem, String rackId) throws RemoteDataAccessException;
	
	/**
	 * This will check if all the items are in sync.
	 * @param PIQItem list of items that needs to be checked against PIQ
	 * @throws RemoteDataAccessException 
	 */
	public void areDevicesInSync(List<PIQItem> piqItems) throws RemoteDataAccessException;
	
	/**
	 * Get the items not in sync
	 * @param deviceId the ID of the device in PIQ that we are trying to move
	 * @param rackId the ID of the rack in PIQ to move the device
	 */
	public PIQItemNotInSync getPIQItemsInSync();
	
	/**
	 * Update the external key for the rack item
	 * @param rackItem rack item
	 * @param reset Should we reset to the original PIQ generated key?
	 * @param piqIdForReset TODO
	 * @return
	 * @throws RemoteDataAccessException 
	 */
	
	public String updateExternalKey(Item rackItem, boolean reset, String piqIdForReset) throws RemoteDataAccessException;
}
