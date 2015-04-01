package com.raritan.tdz.piq.home;

import java.util.List;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.exceptions.InstalledPDUWithoutIPException;
import com.raritan.tdz.piq.home.PIQSyncPorts.TYPE;
import com.raritan.tdz.piq.json.PDUIPAddressJSON;

/**
 * This interface provides all the methods required for synchronizing a dcTrack PDU with PIQ.
 * @author Andrew Cohen
 */
public interface PIQSyncPDUClient extends PIQRestClient {
	
	
	/**
	 * Add a PDU from dcTrack database to PowerIQ database
	 * @param dataPort TODO
	 * @param ipAddress TODO
	 * @param jobData TODO
	 * @param Item - RackPDU item 
	 * @return String - Power IQ job ID
	 * @throws RemoteDataAccessException 
	 * @throws InstalledPDUWithoutIPException
	 */
	public String addRPDU(Item pduItem, DataPort dataPort, String ipAddress) throws RemoteDataAccessException, InstalledPDUWithoutIPException;
	
	/**
	 * Update a PDU from dcTrack database to PowerIQ database
	 * @param dataPort TODO
	 * @param ipAddress TODO
	 * @param Item - RackPDU item 
	 * @return String - Power IQ job ID
	 * @throws RemoteDataAccessException 
	 * @throws InstalledPDUWithoutIPException
	 */
	public String updateRPDU(Item pduItem, DataPort dataPort, String ipAddress) throws RemoteDataAccessException, InstalledPDUWithoutIPException;
	
	/**
	 * Checks to see if given RPDU (ID) exists in PIQ
	 * @param String - powerIQ ID
	 * @throws RemoteDataAccessException 
	 */
	public boolean isRPDUInSync(String pduId) throws RemoteDataAccessException;
	
	/**
	 * Delete a PDU from PowerIQ.
	 * @param pduId the ID of the PDU in PIQ
	 */
	public void deletePDU(String pduId) throws RemoteDataAccessException;
	
	/**
	 * Move a PDU to a different Rack.
	 * @param pduId the ID of the Rack PDU in PIQ that we are trying to move
	 * @param rackId the ID of the rack in PIQ to move the device
	 */
	public void moveRPDUTo(String deviceId, String rackId) throws RemoteDataAccessException;
	
	/**
	 * This will check if all the items are in sync.
	 * @param PIQItem list of items that needs to be checked against PIQ
	 * @throws RemoteDataAccessException 
	 */
	public void areRPDUsInSync(List<PIQItem> piqItems) throws RemoteDataAccessException;
	
	/**
	 * Get the items not in sync
	 * @param deviceId the ID of the device in PIQ that we are trying to move
	 * @param rackId the ID of the rack in PIQ to move the device
	 */
	public PIQItemNotInSync getPIQItemsInSync();
	
	/**
	 * Update the external key for the rack item
	 * @param ipAddress TODO
	 * @param piqIdForReset TODO
	 * @param reset Should we reset to the original PIQ generated key?
	 * @param rackItem rack item
	 * @return
	 * @throws RemoteDataAccessException 
	 */
	
	public String updateExternalKey(Item pduItem, String ipAddress, String piqIdForReset, boolean reset) throws RemoteDataAccessException;
	
	/**
	 * Checks to see if PDU's proxy Index is in sync
	 * @Param pduItem the Item that needs to be checked against PDU
	 * @return true if proxy index is in sync
	 * @throws RemoteDataAccessException 
	 */
	public boolean isProxyIndexInSync(Item pduItem) throws RemoteDataAccessException;
	
	/**
	 * Lookup a PDU in Power IQ by IP Address.
	 * @param ipAddress
	 * @return the piq id of the PDU, or null if there is no PDU with the specified IP Address
	 */
	public Integer lookupByIPAddress(String ipAddress) throws RemoteDataAccessException;
	
	/**
	 * Lookup a PDU in Power IQ by IP Address and Proxy Index.
	 * @param ipAddress
	 * @param proxyIndex
	 * @return the piq id of the PDU, or null if there is no PDU with the specified IP Address and Proxy Index
	 * @throws RemoteDataAccessException
	 */
	public Integer lookupByIPAddressAndProxyIndex(String ipAddress, String proxyIndex) throws RemoteDataAccessException;
	
	/**
	 * Performs some post processing on newly added rack PDU to ensure power and sensor ports are linked.
	 * This method does NOT need to be invoked when calling addRPDU since it will be internally invoked.
	 * However, if you want to associate a Rack PDU in dcTrack with an existing PDU in Power IQ, this method
	 * should be invoked to ensure that power and sensor ports are linked. 
	 *  
	 * @param pduItem the PDU item in dcT
	 * @param piqId the piq_id to link the item with
	 * @throws RemoteDataAccessException
	 */
	public void postProcessAddPDU(Item probeItem, Item item, Integer piqId) throws RemoteDataAccessException;
	
	/**
	 * Force PIQ to rescan the specified PDU.
	 * @param pduId the piq ID of the PDU
	 * @return the overall health or null if unknown
	 */
	public String rescan(Integer pduId) throws RemoteDataAccessException;
	
	/**
	 * Synchronize PDU sensor readings between (PIQ and DCTrack)
	 * After implementing US2017, this function will dynamically add sensors. 
	 * @throws DataAccessException, RemoteDataAccessException, BusinessValidationException 
	 * @throws Exception TODO
	 * @throws Exception 
	 * @Param pduItems - the list of pdu Item for which readings need to be synced
	 */
	public List<String> syncPduReadings (List<Item> pduItems)throws DataAccessException, RemoteDataAccessException, Exception;
	
	
	/**
	 * Synchronize PDU sensor readings between (PIQ and DCTrack)
	 * @param piqHost TODO
	 * @param locationName TODO
	 * @param types TODO
	 * @throws Exception TODO
	 * @throws Exception 
	 * @Param pduItems - the list of pdu Item for which readings need to be synced
	 */
	public List<String> syncAllPduReadings(String piqHost, String locationName, List<TYPE> types)throws Exception;
	
	
	/**
	 * update IP address of the rack PDU in PIQ
	 * @param String ip
	 * @param DataPort dPort
	 * @throws RemoteDataAccessException
	 */
	public void updateIpAddress (String ip, DataPort  dPort) throws RemoteDataAccessException;
	
	/**
	 * update IP address of the rack PDU in PIQ
	 * @param String oldIpAddress
	 * @param String newIpAddress
	 * @throws RemoteDataAccessException
	 */
	public void updateIpAddress (String oldIpAddress, String newIpAddress) throws RemoteDataAccessException;
	
	/**
	 * update IP address of the rack PDU in PIQ
	 * @param PDUIPAddressJSON ips 
	 * @throws RemoteDataAccessException
	 */
	public void updateIPAddresses (PDUIPAddressJSON ips) throws RemoteDataAccessException;

	/**
	 * This function returns ture if PIQ supports IPAddressChange API
	 * @return  true on success, else false
	 */
	public boolean doesPIQSupportIpAddressChangeAPI ();
	
	/**
	 * Check if item has proxy index;
	 * @param item
	 * @return true on success, else false
	 */
	public boolean isProxiedItem (Item item);
	
	/**
	 * check it PIQ is REST API service is responding.
	 * This function waits for 30 seconds before sending rest request.  
	 * If the request fails it will try 3 more time with a delay of 30 seconds
	 * 
	 * @return true on success, else false
	 */
	public boolean isPIQResponding();

	/**
	 * check if the PDU is integrated with PIQ.
	 * @param ipAddress
	 * @param piqId
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public boolean isPduIntegratedWithPIQ(String ipAddress, Integer piqId)
			throws RemoteDataAccessException;
	
}
