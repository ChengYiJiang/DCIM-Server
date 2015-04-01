package com.raritan.tdz.circuit.home;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

public interface DataCircuitHome {
	/**
	 * Fetch data circuit based on criteria
	 * @param CircuitCriteriaBase
	 * @return List of Data Circuits
	 * @throws DataAccessException
	 */
	public List<DataCircuit> viewDataCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException, BusinessValidationException;
		
	/**
	 * Adds a new Data circuit in the system.
	 * @param dataCircuit
	 * @param origUser when invoked via updatePowerCircuit, then this is original user who created the circuit
	 * @param origDate when invoked via updatePowerCircuit, then this is original circuit creation date
	 * @return New Data Circuit ID
	 * @throws DataAccessException
	 */
	public long addDataCircuit(DataCircuit dataCircuit, String origUser, Timestamp origDate) throws DataAccessException, BusinessValidationException;
	
	/**
	 * Delete the data circuits from the system
	 * @param circuitIdsToBeDeleted
	 * @throws DataAccessException
	 */
	public Long deleteDataCircuitByIds(Collection<Long> circuitIdsToBeDeleted, boolean isUpdate) throws DataAccessException, BusinessValidationException;
		

	/**
	 * Update Data circuit in the system.
	 * @param dataCircuit
	 * @return 
	 * @throws DataAccessException
	 */
	public long updateDataCircuit(DataCircuit dataCircuit) throws DataAccessException, BusinessValidationException;	
	
	DataConnection getConnForPanel(Long panelPortId) throws DataAccessException;
	DataConnection getPanelToPanelConn(Long portId) throws DataAccessException;
		
	/**
	 * Validates a circuit has a legal structure.
	 * @param circuit
	 * @throws DataAccessException
	 * @throws BusinessValidationException if circuit is not valid
	 */
	public void validateCircuit(DataCircuit circuit) throws DataAccessException, BusinessValidationException;
	
	void deleteItemDataConnections(long itemId) throws DataAccessException;

	boolean isLogicalConnectionsExist(long itemId, long itemId2)
			throws DataAccessException;

	
	/**
	 * Get data circuit id for data port Id that is the start of a circuit 
	 * @param portId - id of port for which to find the fanout circuit
	 * @return data circuit Id of fanout circuit
	 */	
	public Long getFanoutCircuitIdForStartPort(long portId);

	

}

	
