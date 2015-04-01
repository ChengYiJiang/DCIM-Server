package com.raritan.tdz.circuit.request;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitRequestInfo;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author Santo Rosario
 * This will handle the requests to the database in terms of circuit request
 * 
 */
public interface CircuitRequest {

	/**
	 * Create Connect Request transaction (new connection request)  
	 * @author Santo Rosario
	 * @param circuitView - circuit to be installed
	 * @return - New Request Id
	 */	
	public long connect(CircuitViewData circuitView);
	
	/**
	 * Create Disconnect Request transaction  
	 * @author Santo Rosario
	 * @param circuitView - circuit to be disconnected
	 * @return - New Request Id
	 */
	public long disconnect(CircuitViewData circuitView, String linkRequestNo);

	/**
	 * Delete a Request  
	 * @author Santo Rosario
	 * @param requestId - ID of request to be delete  
	 * @param doAssociatedRequests - not use right now
	 * @return none
	 */			
	public void delete(Long requestId, boolean doAssociatedRequests);
	

	/**
	 * Get Requests associated with circuit  
	 * @author Santo Rosario
	 * @param requestId - ID of request to be delete  
	 * @param doAssociatedRequests - not use right now
	 * @return none
	 */			
	public List<Request> getRequestForCircuit(CircuitViewData circuitView);

	public void updateCircuitStatus(CircuitViewData circuitView, Long statusValueCode) throws Throwable ;

	public void updateCircuitStatus(Long circuitId, String tableName, Long statusValueCode)  throws Throwable; 	
	
	public List<Long> getDataCircuitIdsForWorkOrder(Long workOrderId);
	public List<Long> getPowerCircuitIdsForWorkOrder(Long workOrderId);

	public void archiveWorkOrder(Request request, CircuitViewData circuitView, UserInfo userInfo) throws Throwable;

	public void checkForPendingRequest(List<CircuitViewData> cirList) throws BusinessValidationException;
	
	/**
	 * Create one or more request(s) to disconnect connections associated with an item 
	 * @param requestId
	 * @param requestType TODO
	 * @return
	 * @throws DataAccessException
	 * @throws BusinessValidationException 
	 */
	public void disconnectAllRequest(Long requestId, String requestType) throws DataAccessException, BusinessValidationException;

	/**
	 * get list of planned circuits per port class (DATA, POWER)
	 * @param itemId
	 * @param requestType
	 * @return
	 * @throws DataAccessException
	 */
	public Map<Long, List<Long>> getPlannedCircuits(Long itemId, String requestType)
			throws DataAccessException;


	boolean hasConnectionRequests(Long itemId, Errors errors);

	/**
	 * get list of circuits for given item ids
	 * @param itemIdList
	 * @param requestType
	 * @return
	 * @throws DataAccessException
	 */
	public Map<Long, List<Long>> getPlannedCircuits(List<Long> itemIdList,
			String requestType) throws DataAccessException;

	public void disconnectCircuits(List<CircuitCriteriaDTO> circuitIds, Long requestId, String requestType, Boolean chassisChanged) 	throws DataAccessException, BusinessValidationException;
	
}
