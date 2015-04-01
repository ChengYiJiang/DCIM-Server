package com.raritan.tdz.circuit.home;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ConnectionToMove;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.home.ItemObjectFactory;
import com.raritan.tdz.util.RequestDTO;

public interface CircuitPDHome {
	/**
	 * Fetch list of data/power circuit for list view 
	 * @param circuitType, could be 'data', 'power' or null
	 * @return List of Data/Power Circuits, no connection details
	 * @throws DataAccessException
	 */
	public List<CircuitViewData> viewCircuitPDList(CircuitCriteriaDTO cCriteria) throws DataAccessException;
	
	public CircuitViewData getCircuitViewData(CircuitCriteriaDTO cCriteria) throws DataAccessException;
		
	/**
	 * Fetch circuit by criteria.
	 * @param cCriteria
	 * @return
	 * @throws DataAccessException
	 */
	public List<? extends ICircuitInfo> viewCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException, BusinessValidationException;
	
	/**
	 * Get list of ports that are part of circuits.
	 * @param none
	 * @return List of connected ports
	 * @throws DataAccessException
	 */	
	public List<ConnectedPort> viewAllConnectedPorts() throws DataAccessException;
	
	/**
	 * @return an item object factory used to create business wrappers for item domain objects.
	 */
	public ItemObjectFactory getItemObjectFactory();

	
	public HashMap<Long, CircuitRequestInfo> getCircuitRequestInfo(Long circuitType, boolean useProposeCircuitId);

	CircuitRequestInfo getRequestInfoForCircuit(Long circuitId, Long circuitType, boolean useProposeCircuitId);

	/**
	 * Saves a proposed circuit.
	 * @param circuitId the original circuit ID 
	 * @param connReqs a list of changes
	 * @param Long newCircuitId the proposed circuit ID to use. if null, will assign a new circuit ID.
	 * @throws DataAccessException
	 */
	public ProposedCircuitInfo saveProposedCircuit(long circuitId, ConnectionRequest connReq, Long newCircuitId) throws DataAccessException;
	
	
	/**
	 * Deletes a proposed circuit.
	 * @param proposedCircuitId the proposed circuit ID
	 * @throws DataAccessException 
	 */
	public ProposedCircuitInfo deleteProposedCircuit(CircuitDTO circuit) throws DataAccessException, BusinessValidationException;
	
	
	DataConnection getConnForPanel(Long panelPortId) throws DataAccessException;
	DataConnection getPanelToPanelConn(Long portId) throws DataAccessException;
	
	/**
	 * Checks if a particular data circuit is a partial circuit that is currently in use.
	 * @param circuitInfo the circuit info
	 * @return true only if the data circuit is a partial circuit AND is currently in use.
	 * @throws DataAccessException
	 */
	public boolean isPartialCircuitInUse(ICircuitInfo circuitInfo) throws DataAccessException;
	
	/**
	 * Get all shared data circuit traces.
	 * @return
	 * @throws DataAccessException
	 */
	public Set<String> getSharedDataCircuitTraces() throws DataAccessException;
	
	/**
	 * Get all shared power circuit traces.
	 * @return
	 * @throws DataAccessException
	 */
	public Set<String> getSharedPowerCircuitTraces() throws DataAccessException;
	
	/**
	 * Validate if circuit can be disconnected. If it is shared and cannot disconnect, thrown an exception
	 * 
	 * @param circuitInfo
	 * @return true when circuit can be disconnected
	 * @throws BusinessValidationException 
	 * @throws DataAccessException 
	 */
	public boolean validateCircuitForDisconnect(ICircuitInfo circuitInfo) throws DataAccessException, BusinessValidationException;
	HashMap<Long, String> getConnIdWihCircuitTrace(Long circuitType);
	
	/**
	 * Compares an original and proposed circuit to determine a list of connection state changes (for Change Management).
	 * @param origCircuit the original circuit
	 * @param proposedCircuit the proposed data circuit
	 * @return a connection request reflecting all of the circuit changes
	 */
	public ConnectionRequest diffCircuit(ICircuitInfo origCircuit, ICircuitInfo proposedCircuit) throws DataAccessException, BusinessValidationException;
	
	/**
	 * Validates a circuit has a legal structure.
	 * @param circuit
	 * @throws DataAccessException
	 * @throws BusinessValidationException if circuit is not valid
	 */
	public void validateCircuit(ICircuitInfo circuit) throws DataAccessException, BusinessValidationException;
	
	/**
	 * Locks the ports associated with a circuit connection.
	 * @param connection
	 */
	public boolean lockConnection(ICircuitConnection connection) throws DataAccessException;
	
	/**
	 * Unlocks the ports associated with a circuit connection.
	 * @param connection
	 */
	public void unlockConnection(ICircuitConnection connection) throws DataAccessException;
	
	/**
	 * Loads the original connection associated with a connection to move.
	 * @param connToMove the connection to move
	 * @return
	 */
	public ICircuitConnection getConnection(ConnectionToMove connToMove) throws DataAccessException;
	
	/**
	 * Fetch the proposed circuit ID for the specified circuit.
	 * @param circuit the circuit
	 * @return the proposed circuit ID or null if no proposed circuit exists
	 */
	public Long getProposedCircuitId(ICircuitInfo circuit) throws DataAccessException;
	
	/**
	 * Fetch the original circuit ID for the specified proposed circuit.
	 * @param circuit the circuit
	 * @return the proposed circuit ID or null if no proposed circuit exists
	 */
	public CircuitUID getOriginalCircuitIdForProposed(long proposedCircuitId, long circuitType) throws DataAccessException, BusinessValidationException;
	
	/**
	 * Returns the original installed circuit associated with the proposed circuit ID.
	 * @param proposedCircuitId
	 * @param circuitType
	 * @return
	 */
	public ICircuitInfo getOriginalCircuitForProposed(long proposedCircuitId, long circuitType) throws DataAccessException, BusinessValidationException;
	
	HashMap<Long, Long> getProposeCircuitPortsNetWatts();
	
	public long getCircuitTotalCount() throws DataAccessException;
	
	public boolean canOperateOnCircuit(CircuitViewData circuitViewData, boolean isProposedCkt) throws DataAccessException;
	
	public boolean canCreateCircuit();

	public Map<String, Object> createCircuitRequest(CircuitPDService circuitService,
			Collection<CircuitCriteriaDTO> requestList, boolean warningConfirmed)
			throws ServiceLayerException;

	public List<RequestDTO> postProcessRequest(CircuitPDService circuitService,
			Map<String, Object> requestMap) throws ServiceLayerException;

	public Map<String, Object> saveCircuitWithDTO(CircuitPDService circuitService,
			CircuitDTO circuit) throws ServiceLayerException;
	
}

	
