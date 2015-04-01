package com.raritan.tdz.circuit.dao;

import java.util.HashMap;
import java.util.List;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.DataAccessException;

public interface DataCircuitDAO extends Dao<DataCircuit> {

	/**
	 * Get a data circuit from database using circuit id and lazy load it
	 * @param circuitId - circuit Id of circuit to be loaded
	 * @return DataCircuit object 
	 */
	public DataCircuit getDataCircuit(Long circuitId) throws DataAccessException;
	
	/**
	 * Get a list of data circuit from database using circuit id
	 * @param connectionId - connection Id of circuit to be loaded
	 * @return List of DataCircuit objects 
	 */
	public List<DataCircuit> viewDataCircuitByConnId(Long connectionId) throws DataAccessException;
	
	/**
	 * Get a list of date circuit from database using circuit id
	 * @param connectionId - port Id of circuit to be loaded
	 * @return List of DataCircuit objects 
	 */
	public List<DataCircuit> viewDataCircuitByStartPortId(Long portId)	throws DataAccessException;	
	
	/**
	 * Get a list of data circuit from database using circuit id
	 * @param cCriteria - CircuitCriteriaDTO object
	 * @return List of DataCircuit objects 
	 */
	public List<DataCircuit> viewDataCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException;
	

	/**
	 * Get list of items with port that are the destination connection for a given itemId
	 * @param itemId - id of item for which to find destination ports/items
	 * @return List of item objects
	 */	
	public HashMap<Long, PortInterface> getDestinationItemsForItem(long itemId);
	
	/**
	 * Get list of ports that have an associated proposed circuit id for a given item
	 * @param itemId - id of item for which to find destination ports/items
	 * @return List of DataPortDTO objects
	 */	
	public HashMap<Long, PortInterface> getProposedCircuitIdsForItem(long itemId);
	
	/**
	 * Get data circuit id for data port Id that is the start of a circuit 
	 * @param portId - id of port for which to find the fanout circuit
	 * @return data circuit Id of fanout circuit
	 */	
	public Long getFanoutCircuitIdForStartPort(long portId);

	/**
	 * Check is an item has logical connections. If itemId2 is not null, check against that item 
	 * @param itemId1 - item id of item to check for logical connections
	 * @param itemId2 - item id of item to check if it is connected to itemid1 for logical connections
	 * @return true or false
	 */	
	public boolean isLogicalConnectionsExist(Long itemId1, Long itemId2) throws DataAccessException;

	/**
	 * get the data circuit using the start and end port id
	 * @param startPortId
	 * @param endPortId
	 * @return
	 * @throws DataAccessException
	 */
	public DataCircuit viewDataCircuitByPortIds(Long startPortId, Long endPortId)
			throws DataAccessException;

	public String getCircuitTrace(Long startPortId);

	public CircuitViewData getDataCircuitForStartPort(String location,	String itemName, String portName) throws DataAccessException;

	public Long getProposedCircuitId(Long circuitId) throws DataAccessException;

	CircuitViewData getDataCircuitForStartPortId(Long portId) throws DataAccessException;

}
