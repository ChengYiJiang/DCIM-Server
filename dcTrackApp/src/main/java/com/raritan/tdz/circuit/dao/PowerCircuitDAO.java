package com.raritan.tdz.circuit.dao;

import java.util.HashMap;
import java.util.List;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.PowerBankInfo;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.DataAccessException;

public interface PowerCircuitDAO extends Dao<PowerCircuit> {


	/**
	 * Get a list of power circuit from database using circuit id
	 * @param connectionId - circuit Id of circuit to be loaded
	 * @return List of PowerCircuit objects
	 */
	public List<PowerCircuit> viewPowerCircuitByConnId(Long connectionId) throws DataAccessException;

	/**
	 * Get a list of date circuit from database using circuit id
	 * @param connectionId - circuit Id of circuit to be loaded
	 * @return List of PowerCircuit objects
	 */
	public List<PowerCircuit> viewPowerCircuitByStartPortId(Long connectionId)	throws DataAccessException;

	/**
	 * Get a list of power circuit from database using circuit id
	 * @param cCriteria - CircuitCriteriaDTO object
	 * @return List of PowerCircuit objects
	 */
	public List<PowerCircuit> viewPowerCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException;

	/**
	 * Get list of items with port that are the destination connection for a given itemId
	 * @param itemId - id of item for which to find destination ports/items
	 * @return List of objects
	 */
	public HashMap<Long, PortInterface> getDestinationItemsForItem(long itemId);

	/**
	 * Get list of power ports for a given item with the actual amps for the next node in circuit
	 * @param itemId - id of item for which to find destination ports/items
	 * @return List of objects
	 */
	public HashMap<Long, PortInterface> getNextNodeAmpsForItem(long itemId);

	/**
	 *
	 * @param bankId
	 * @return
	 */
	public PowerBankInfo getPowerBankInfo(long bankId);


	/**
	 * Get the powerUsage view list based on the HQL query string and arguments.
	 * Please note that the list returned can be either a PowerUsageView or
	 * PowerAllView
	 * @param queryArgs
	 * @param namedQuery
	 * @return
	 */
	public List<?> getPowerUsage(String queryStr, Object[] queryArgs);

	/**
	 * Gets the power circuit loaded with power connections list.
	 * This method will read the trace and get corresponding connections
	 * in the transient member circuitConnections of PowerCircuit
	 * @param powerCircuitId
	 * @return
	 */
	public PowerCircuit getPowerCircuit(long powerCircuitId);


	public long getPowerWattUsedTotal(long portPowerId, Long fuseLkuId);

	/**
	 * update the circuit trace, shared circuit trace and the end connection when the connection is updated
	 * @param oldPort
	 * @param newPort
	 * @throws DataAccessException
	 */
	public void changeCircuitConnectionChange(PowerPort oldPort, PowerPort newPort) throws DataAccessException;

	/**
	 * get the list of circuits that ends with the trace provided
	 * @param trace
	 * @return
	 */
	public List<PowerCircuit> getCircuitsWithTrace(String trace);

	/**
	 * get the circuit id, circuit trace and shared circuit trace
	 * @param trace
	 * @return
	 */
	public List<Object[]> getCircuitsInfoWithTrace(String trace);

	/**
	 * gets the connection and the destination port for a given port id
	 * @param portId
	 * @return
	 */
	List<Object[]> getConnAndDestPort(Long portId);

	/**
	 * updates the circuit trace with the information provided
	 * @param circuitId
	 * @param circuitTrace
	 * @param sharedCircuitTrace
	 * @param endConnId
	 */
	void changeCircuitTrace(Long circuitId, String circuitTrace,
			String sharedCircuitTrace, Long endConnId);

	/**
	 * Get list of ports that have an associated proposed circuit id for a given item
	 * @param itemId - id of item for which to find destination ports/items
	 * @return List of PowerPortDTO objects
	 */
	public HashMap<Long, PortInterface> getProposedCircuitIdsForItem(long itemId);

	public List<PowerWattUsedSummary> getPowerWattUsedSummary(long portPowerId, Long portIdToExclude, Long fuseLkuId, Long inputCordToExclude, boolean measured);

	public List<PowerWattUsedSummary> getPowerWattUsedSummary(long portPowerId,	Long portIdToExclude, Long fuseLkuId, Long inputCordToExclude);

	public List<PowerWattUsedSummary> getPowerWattUsedSummaryMeasured(long portPowerId);


	/**
	 * reconnect the port and build new circuit trace
	 * @param oldSrcPortId
	 * @param oldDestPortId
	 * @param newSrcPortId
	 * @param newDestPortId
	 * @return
	 */
	public void reconnectPowerPorts (Long oldSrcPortId, Long oldDestPortId, Long newSrcPortId, Long newDestPortId);

	/**
	 * Delete a power circuit
	 * @param isUpdate TODO
	 * @param power circuit id
	 * @return 0 - if success, -1 circuit in used by another circuit, -2 circuit does not exists
	 */
	Long deleteCircuit(Long circuitId, boolean isUpdate);

	public void processRelatedPowerCircuits (Long startPortId, String oldTrace);

	public String getCircuitTrace(Long startPortId);

	public CircuitViewData getPowerCircuitForStartPort(String location, String itemName, String portName) throws DataAccessException;

	public Long getProposedCircuitId(Long circuitId) throws DataAccessException;

}
