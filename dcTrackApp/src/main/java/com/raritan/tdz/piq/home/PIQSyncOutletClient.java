package com.raritan.tdz.piq.home;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.Outlet;


/**
 * An interface for synchronizing power connections in dcTrack with those in PIQ.
 * @author Andrew Cohen
 */
public interface PIQSyncOutletClient extends PIQRestClient {

	/**
	 * Updates a power connection between a Device and a PDU outlet in PIQ.
	 * It will remove any previous connection between a device and that same PDU outlet in PIQ.
	 * @param sourcePort the source port of the item in dcTrack - must be of a Device/Probe/Network
	 * @param destPort the destination port of the PDU item in dcTrack
	 */
	public void updatePowerConnection(PowerPort sourcePort, PowerPort destPort) throws RemoteDataAccessException;
	

	/**
	 * Deletes a power connection on a PDU outlet in PIQ.
	 * @param destPort the destination port of the PDU item in dcTrack
	 */
	public void deletePowerConnection(PowerPort destPort) throws RemoteDataAccessException;
	
	/**
	 * Find a specific outlet on a PDU in PIQ.
	 * @param pduPiqId the PIQ ID of the PDU
	 * @param outletNumber the outlet number respective to other outlets on the same PDU
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public Outlet findOutlet(long pduPiqId, int outletNumber) throws RemoteDataAccessException;
	
	/**
	 * Gets a specific outlet by its unique id in PIQ.
	 * @param outletId the outlet id
	 * @return the outlet or null if it doesn't exist for the id
	 * @throws RemoteDataAccessException
	 */
	public Outlet getOutlet(long outletId) throws RemoteDataAccessException;
	
	/**
	 * This will check if all the power associations are in sync with Power IQ
	 * @param powerConnections list of power connections that needs to be checked against PIQ
	 * @throws RemoteDataAccessException 
	 */
	public PIQAssociationNotInSync areAssociationsInSync(List<PowerConnection> powerConnections, List<Integer> piqPduIds) throws RemoteDataAccessException;
	
	/**
	 * Get the items not in sync
	 * @param deviceId the ID of the device in PIQ that we are trying to move
	 * @param rackId the ID of the rack in PIQ to move the device
	 */
	public PIQAssociationNotInSync getPIQAssociationNotInSync();

	/**
	 * Get the latest reading in amps for PDU outlet 
	 * @param pduPiqId - the PIQ ID of the PDU
	 * @param outletNumber - the outlet number on PDU for which the current_amps reading is requested.
	 * @return - null on failure. 
	 *           Double value if success. 
	 * @throws RemoteDataAccessException
	 */
	public Double getOutletCurrentReading(long pduPiqId, int outletId) throws RemoteDataAccessException;

	/**
	 * Get the latest reading in Amps for outlet 
	 * @param pduPiqId - the PIQ ID of the outlet
	 * @return - null on failure. 
	 *           Double value if success. 
	 * @throws RemoteDataAccessException
	 */
	public Double getOutletCurrentReading(long pduPiqId) throws RemoteDataAccessException;
	
}
