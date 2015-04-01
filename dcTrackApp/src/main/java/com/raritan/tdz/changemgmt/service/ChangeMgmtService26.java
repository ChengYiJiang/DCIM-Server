package com.raritan.tdz.changemgmt.service;

import java.util.List;

import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.ServiceLayerException;

/**
 * A service exposed via BlazeDS to perform change management operations.
 * 
 * @author Andrew Cohen
 * @deprecated To be replaced by new Change Management Service interface in 3.0
 * @version 2.6.1
 */
public interface ChangeMgmtService26 {

	/**
	 * Create Disconnect Request transaction  
	 * @author Santo Rosario
	 * @param itemId - the itemId of port to be disconnected
	 * @param connList - List of connections ID that makeup the circuit
	 * @param portClassValueCode - Port Class from dct_lks_data.lkp_value_code
	 * @param portName - Name of port to be disconnected.
	 * @return - New Request Id
	 * @throws ServiceLayerException
	 */
	public long disconnectRequest(CircuitViewData circuitView) throws ServiceLayerException;

	/**
	 * Create Disconnect And Move Request transaction  
	 * @author Santo Rosario
	 * @param itemId - is the itemId of port to be disconnected
	 * @param connList - List of connections ID that makeup the circuit
	 * @param portClassValueCode - Port Class from dct_lks_data.lkp_value_code
	 * @param portName - Name of port to be disconnected.
	 * @return - New Request Id
	 * @throws ServiceLayerException
	 */	
	public long disconnectAndMoveRequest(Long itemId, List<Long> connList, long portClassValueCode, String portName) throws ServiceLayerException;

	/**
	 * Create Connect Request transaction (new connection request)  
	 * @author Santo Rosario
	 * @param itemId - is the itemId of port to be disconnected
	 * @param circuitId - Circuit Id for the connection
	 * @param portClassValueCode - Port Class from dct_lks_data.lkp_value_code
	 * @param portName - Name of port to be disconnected.
	 * @return - New Request Id
	 * @throws ServiceLayerException
	 */	
	public long connectRequest(CircuitViewData circuitView) throws ServiceLayerException;

	/**
	 * Create Re-Connect Request transaction  
	 * @author Santo Rosario
	 * @param itemId - is the itemId of port to be disconnected
	 * @param newCircuitId - Temporary Circuit Id for the new connection to be created
	 * @param portClassValueCode - Port Class from dct_lks_data.lkp_value_code
	 * @param portName - Name of port to be disconnected.
	 * @return - New Request Id
	 * @throws ServiceLayerException
	 */		
	public long reconnectRequest(Long itemId, Long newCircuitId, long portClassValueCode, String portName) throws ServiceLayerException;
	
	/**
	 * Approve a Request  
	 * @author Santo Rosario
	 * @param requestId - ID of request to be delete  
	 * @param doAssociatedRequests - not use right now
	 * @return none
	 * @throws ServiceLayerException
	 */		
	
	public void deleteRequest(Long requestId, boolean doAssociatedRequests) throws ServiceLayerException;
	

	/**
	 * Get Request  
	 * @author Santo Rosario
	 * @param Request Object  
	 * @return - Request Object List
	 * @throws ServiceLayerException
	 */	
	public List<Request> viewRequest(Request request) throws ServiceLayerException;
	
	/**
	 * Get Request stage  
	 * @author Santo Rosario
	 * @param Request Object  
	 * @return - long 
	 * @throws ServiceLayerException
	 */	
	public long getRequestStage(Request request) throws ServiceLayerException; 	
}

//