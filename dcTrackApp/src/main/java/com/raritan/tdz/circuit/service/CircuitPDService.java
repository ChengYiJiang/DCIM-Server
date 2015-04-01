package com.raritan.tdz.circuit.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitListDTO;
import com.raritan.tdz.circuit.dto.ConnectedPortDTO;
import com.raritan.tdz.circuit.dto.StructureCableDTO;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.util.RequestDTO;
import com.raritan.tdz.views.ItemObject;
import com.raritan.tdz.views.ItemPortObject;

public interface CircuitPDService {
	/**
	 * Fetch a circuit based on criteria
	 * @param CircuitCriteriaBase
	 * @return List of Circuits
	 * @throws ServiceLayerException
	 */
	public List<CircuitDTO> viewCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws ServiceLayerException;

	/**
	 * Adds a new Power circuit in the system.
	 * @param powerCircuit
	 * @return New Power Circuit ID
	 * @throws ServiceLayerException
	 */
	//public long addPowerCircuit(CircuitDTO powerCircuit) throws ServiceLayerException;
	//public long addCircuit(CircuitDTO circuit) throws ServiceLayerException;
	public CircuitDTO saveCircuit(CircuitDTO circuit) throws ServiceLayerException;
	/**
	 * Delete the power circuits from the system
	 * @param circuitIdsToBeDeleted
	 * @throws ServiceLayerException
	 */
	//public long deletePowerCircuitByIds(Collection<Integer> circuitIdsToBeDeleted) throws ServiceLayerException;
	public Long deleteCircuitByIds(Collection<CircuitCriteriaDTO> cCriteriaList) throws ServiceLayerException;
	
	/**
	 * Delete the structured cabling connections from the system
	 * @param structuredCablingList
	 * @throws ServiceLayerException
	 */
	public Long deleteStructuredCablingConnection(Collection<StructureCableDTO> structuredCablingList) throws ServiceLayerException;

	/**
	 * Fetch list of data/power circuit for list view
	 * @param circuitType
	 * @return List of Data/Power Circuits, not connection details
	 * @throws DataAccessException
	 */
	public List<CircuitListDTO> viewCircuitPDList(CircuitCriteriaDTO cCriteria) throws ServiceLayerException;
	
	/**
	 * Get the total number of circuits for all sites.
	 * @return
	 */
	public long getCircuitTotalCount() throws ServiceLayerException;
		
	public List<ConnectedPortDTO> viewAllConnectedPorts() throws ServiceLayerException;
	
	public List<RequestDTO> createRequestForCircuit(Collection<CircuitCriteriaDTO> requestList) throws ServiceLayerException;

	/**
	 * user has confirmed to the warning message on circuit request
	 */
	public List<RequestDTO> createRequestForCircuitConfirmed(Collection<CircuitCriteriaDTO> requestList) throws ServiceLayerException;

	//
	// NOTE - the below methods are deprecated since they have been moved to ItemService.
	//

	/**
	 * @deprecated moved to ItemService.
	 * @param cabinetId
	 * @return List of items
	 * @throws ServiceLayerException
	 */
	public List<ItemObject> viewItemsForLocation(Long locationId, Long portClassValueCode) throws ServiceLayerException;
	
	/**
	 * @deprecated moved to ItemService.
	 * @param itemId
	 * @return List of data ports
	 * @throws ServiceLayerException
	 */
	public List<DataPortDTO> viewDataPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException;

	/**
	 * @deprecated moved to ItemService.
	 * @param itemId
	 * @return List of power ports
	 * @throws ServiceLayerException
	 */
	public List<PowerPortDTO> viewPowerPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException;

	/**
	 * @deprecated moved to ItemService.
	 * @param itemId
	 * @return List of data/power ports
	 * @throws ServiceLayerException
	 */
	public ItemPortObject viewPortsForItem(Long itemId, boolean freePortOnly, Long portClassValueCode) throws ServiceLayerException;
	
	/**
	 * @deprecated moved to ItemService.
	 * @param itemId
	 * @param freePortOnly
	 * @return
	 * @throws ServiceLayerException
	 */
	public List<PortInterface> viewPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException;

    /**
    * Get the button status of the circuit list and circuit detail
    */
    public String getCircuitButtonStatus( List<Double> CircuitIdList ) throws ServiceLayerException;

    /**
     * creates the circuit request using the circuit criteria DTO list
     * @param requestList
     * @param warningConfirmed 
     * @return
     * @throws ServiceLayerException
     */
	public Map<String, Object> createCircuitRequest( Collection<CircuitCriteriaDTO> requestList, boolean warningConfirmed ) throws ServiceLayerException;

	/**
	 * process the request on request bypass mode if enabled
	 * @param requestMap
	 * @return
	 * @throws ServiceLayerException
	 */
	public List<RequestDTO> postProcessRequest(Map<String, Object> requestMap)
			throws ServiceLayerException;

	public Map<String, Object> saveCircuitWithDTO(CircuitDTO circuit)
			throws ServiceLayerException;

	
	

}
