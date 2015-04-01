package com.raritan.tdz.changemgmt.home;

import java.util.Collection;
import java.util.List;

import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
/**
 * The internal business logic API for change management.
 * This class is NOT exposed directly via BlazeDS but via REST APIs which
 * the workflow connectors will call.
 *
 * @author Andrew Cohen
 *
 */
public interface ChangeMgmtHome26{

	/**
	 * Create a new Item Request transaction  
	 * @author Andrew Cohen
	 * @param ItemId - ID of item for which request will be created  
	 * @return - none
	 * @throws DataAccessException
	 */	
	public long createRequest(long itemId, String reqDesc, String tableName, String reqType) throws DataAccessException;

	/**
	 * Delete Request  
	 * @author Santo Rosario
	 * @param rquestId - ID of request to be deleted  
	 * @param doAssociatedRequests - If TRUE, delete open requests link to this request ID
	 * @return a list of ports affected by the request or null 
	 * @throws DataAccessException
	 */	
	public List<IPortInfo> deleteRequest(Long requestId, boolean doAssociatedRequests) throws DataAccessException;

	/**
	 * 
	 * @param circuit
	 * @param linkRequestNo TODO
	 * @return
	 * @throws DataAccessException
	 */
	public long disconnectRequest(ICircuitInfo circuit, String linkRequestNo) throws DataAccessException;
	
	/**
	 * 
	 * @param itemId
	 * @param connList
	 * @param portClassValueCode
	 * @param portName
	 * @return
	 * @throws DataAccessException
	 */
	public long disconnectAndMoveRequest(Long itemId, List<Long> connList, long portClassValueCode, String portName) throws DataAccessException;
	
	/**
	 * 
	 * @param circuit
	 * @return
	 * @throws DataAccessException
	 */
	public long connectRequest(ICircuitInfo circuit)	throws DataAccessException;
	
	/**
	 * 
	 * @param itemId
	 * @param newCircuitId
	 * @param portClassValueCode
	 * @param portName
	 * @param linkRequestNo TODO
	 * @return
	 * @throws DataAccessException
	 */
	public long reconnectRequest(Long itemId, Long newCircuitId, long portClassValueCode, String portName, String linkRequestNo) throws DataAccessException;
	
	/**
	 * Resubmit Request  
	 * @author Santo Rosario
	 * @param rquestId - ID of request to be re-submitted  
	 * @return TODO
	 * @return - none
	 * @throws DataAccessException
	 */	
	Request reSubmitRequest(long requestId) throws DataAccessException; 
	
	/**
	 * Get Request  
	 * @author Santo Rosario
	 * @param Request Object  
	 * @return - Request Object List
	 * @throws DataAccessException
	 */	
	public List<Request> viewRequest(Request request) throws DataAccessException;
	
	/**
	 * Get Request stage  
	 * @author Santo Rosario
	 * @param Request Object  
	 * @return - long 
	 * @throws DataAccessException
	 */	
	public long getRequestStage(Request request) throws DataAccessException;
	
	/**
	 * Get requests associated with a circuit.
	 * @param circuit
	 * @return
	 */
	public Collection<Long> getRequestsForCircuit(ICircuitInfo circuit) throws DataAccessException;
	
	/**
	 * Get requests associated with a circuit connection.
	 * @param conn
	 * @return
	 * @throws DataAccessException
	 */
	public Collection<Long> getRequestsForConnection(ICircuitConnection conn) throws DataAccessException;
	
	/**
	 * Create different type of request transactions  
	 * @author Santo Rosario
	 * @param circuitId - Circuit Id for the connection
	 * @param connList - List of connections ID that makeup the circuit
	 * @param itemId - is the itemId of port to be disconnected
	 * @param reqDesc - Request description.
	 * @param reqType - Request Type
	 * @param startConnId - Start Connection ID of circuit
	 * @param linkRequestNo TODO
	 * @param portClassValueCode - Port Class from dct_lks_data.lkp_value_code
	 * @return - New Request Id
	 * @throws DataAccessException
	 */		
	public long createConnectionRequest(Long circuitId, List<Long> connList, Long itemId, String reqDesc, String reqType, long portClass, Long startConnId, String linkRequestNo) throws DataAccessException;

	List<Request> getItemRequest(long itemId) throws DataAccessException;

	List<Request> getItemRequest(long itemId, Long[] rStage)
			throws DataAccessException;
}
  