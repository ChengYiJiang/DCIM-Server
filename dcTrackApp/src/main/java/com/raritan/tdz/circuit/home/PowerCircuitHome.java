package com.raritan.tdz.circuit.home;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;

public interface PowerCircuitHome {
	
	/**
	 * Fetch power circuit based on criteria
	 * @param CircuitCriteriaBase
	 * @return List of Power Circuits
	 * @throws DataAccessException
	 */
	public List<PowerCircuit> viewPowerCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException, BusinessValidationException;
		
	/**
	 * Adds a new Power circuit in the system.
	 * @param powerCircuit
	 * @param origUser when invoked via updatePowerCircuit, then this is original user who created the circuit
	 * @param origDate when invoked via updatePowerCircuit, then this is original circuit creation date
	 * @return New Power Circuit ID
	 * @throws DataAccessException
	 */
	public long addPowerCircuit(PowerCircuit powerCircuit, String origUser, Timestamp origDate) throws DataAccessException, BusinessValidationException;
	
	/**
	 * Delete the power circuits from the system
	 * @param circuitIdsToBeDeleted
	 * @throws DataAccessException
	 */
	public Long deletePowerCircuitByIds(Collection<Long> circuitIdsToBeDeleted, boolean deleteAll) throws DataAccessException, BusinessValidationException;
		
	/**
	 * Update Power circuit in the system.
	 * @param powerCircuit
	 * @return 
	 * @throws DataAccessException
	 */
	public long updatePowerCircuit(PowerCircuit powerCircuit) throws DataAccessException, BusinessValidationException;
	
	/**
	 * Validates a circuit has a legal structure.
	 * @param circuit
	 * @throws DataAccessException
	 * @throws BusinessValidationException if circuit is not valid
	 */
	public void validateCircuit(PowerCircuit circuit) throws DataAccessException, BusinessValidationException;
		
	boolean isThreePhase(Long phaseLksId);
	
	double getUpRatedFactorForSite(Long portId);

	double getDeRatedFactorForSite(Long portId);

	List<MeItem> getExtraItemForPowerCircuit(PowerPort breakerPort)	throws DataAccessException;

	void deleteItemPowerConnections(long itemId) throws DataAccessException;

	void deleteItemBuswayConnections(long itemId) throws DataAccessException;

	long getPowerPortUsedWatts(Long powerPortId, Long fuseLkuId);

	/**
	 * delete the connections of the items
	 * @param itemIds
	 * @throws DataAccessException
	 */
	void deleteItemsPowerConnections(List<Long> itemIds)
			throws DataAccessException;

	/**
	 * delete source connections for the item
	 * @param itemIds
	 * @throws DataAccessException
	 */
	void deleteItemsPowerSourceConnections(List<Long> itemIds)
			throws DataAccessException;

	/**
	 * deletes the destination connections of the item
	 * @param itemIds
	 * @throws DataAccessException
	 */
	void deleteItemsPowerDestConnections(List<Long> itemIds)
			throws DataAccessException;

	void validatePowerCircuit(PowerCircuit powerCircuit) throws ServiceLayerException;


	void reconnectPowerPorts(ICircuitInfo proposedCircuit,	CircuitViewData circuitView) throws ServiceLayerException;
	
}

	
