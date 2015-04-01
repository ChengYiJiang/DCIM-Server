package com.raritan.tdz.circuit.dao;

import java.io.Serializable;
import java.util.List;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.request.home.RequestInfo;

public interface CircuitDAO<T extends Serializable> extends Dao<T> {

	/**
	 * informs if all the items in the circuit is in the given cabinet
	 * @param circuitId
	 * @param cabinetId
	 * @return true, if the circuit is in the same cabinet 
	 */
	public boolean isCircuitInCabinet(long circuitId, long cabinetId);

	/**
	 * get list of circuits Id for this item
	 * @param item Id
	 * @return list of CircuitCriteriaDTO
	 */
	public List<CircuitCriteriaDTO> getAssociatedCircuitsForCabinet(long cabinetId);
	
	/**
	 * Get a list of circuit ids going out of the cabinet
	 * @param cabinetId
	 * @return
	 */
	public List<CircuitCriteriaDTO> getAllInstalledCircuitsOutsideCabinet(long cabinetId);

	/**
	 * get list of all circuits that is not moving with the cabinet and has the following criteria
	 * -- passing items above / below or
	 * 	-- passing through item(s) that is not in a given cabinet
	 * -- in planned state
	 * -- do not include circuits that has pending request
	 * 
	 * @param cabinetId
	 * @return
	 */
	public List<Long> getCabinetPlannedCircuitIdsNotMoving(long cabinetId);
	
	/**
	 * get the circuit trace from the given port and of a given type
	 * @param connType
	 * @param startPortId
	 * @return
	 */
	public String getCircuitTrace(String connType, Long startPortId);

	/**
	 * return all parent item that has a move request for the circuit that uses the passed connection ids
	 * @param connectionIds
	 * @return
	 */
	public List<RequestInfo> getParentMoveRequest(List<Long> connectionIds);

	/**
	 * Get a list of all circuit ids going out of the cabinet
	 * @param cabinetId
	 * @return
	 */
	public List<CircuitCriteriaDTO> getAllCircuitsOutsideCabinet(long cabinetId);

	/**
	 * get list of circuit pending request info for the item of the given request type
	 * @param itemIds
	 * @param requestTypeLkpCodes
	 * @return
	 */
	public List<RequestInfo> getPendingCircuitRequestForItems(List<Long> itemIds,
			List<Long> requestTypeLkpCodes);

	/**
	 * get proposed circuit request info for a given list of items
	 * @param itemIds
	 * @return
	 */
	public List<RequestInfo> getProposedCircuitRequest(List<Long> itemIds);
	
}
