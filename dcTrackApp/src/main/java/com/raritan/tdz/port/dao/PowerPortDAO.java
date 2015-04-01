package com.raritan.tdz.port.dao;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dto.BreakerDTO;


/**
 *  @author Santo Rosario
 *  Interface to access Power Port domain object
 */

public interface PowerPortDAO extends PortDAO<PowerPort> {

	/**
	 * Get list of free power ports for an item  
	 * @param itemId - ID of item 
	 * @return  - List of PowerPort objects
	 */	
	List<PowerPort> getFreePortsForItem(Long itemId);

	/**
	 * Get a list of item name that have connected power ports given a list of item Ids 
	 * @param itemIds - List of item ids to check 
	 * @return - List of item name that were found to have one or more connected ports
	 */	
	List<String> getItemNamesWithConnectedPorts(List<Long> itemIds);

	/**
	 * Get a list of breaker ports with rating greater or equal to ampRating. Also it 
	 * checks additional criteria for used, breakerlkpValueCodes and phases. 
	 * @param ampsRating 
	 * @param isUsed
	 * @param breakerLkpValueCodes
	 * @param phases
	 * @param fpduItemId fpduItem to be excluded. -1 means include all breakers
	 * @return - list of BreakerDTO
	 */
	List<BreakerDTO> getBreakers(Long ampsRating, Boolean[] isUsed, Long[] breakerLkpValueCodes, 
			Long[] phases, Long breakerPortId, Long fpduItemId);

    /**
     * Get the FPDU item given the panel branch circuit breaker id.
     * @param breakerPortId
     * @return MeItem : FPDU Item
     */
    public Item getFPDUItemForBreakerPortId(Long breakerPortId);

	/**
	 * get the port with the connections
	 * @param id
	 * @return
	 */
	PowerPort getPortWithConnections(Long id);

	/**
	 * get the port using the itemid and port subclass. This should be a unique port
	 * @param itemId
	 * @param portSubClass
	 * @return
	 */
	PowerPort getPort(Long itemId, Long portSubClass);

	/**
	 * initializes the port connections and item
	 * @param p
	 */
	void initPowerPortsAndConnectionsProxy(PowerPort p);

	/**
	 * get the port with source connections loaded
	 * @param id
	 * @return
	 */
	PowerPort getPortWithSourceConnections(Long id);

	/**
	 * update the used flag based in the connection count
	 * @param portId
	 * @param oldConnectedPortId
	 */
	void changeUsedFlag(Long portId, Long oldConnectedPortId);

	/**
	 * get the value of a requested field directly in the table 
	 * @param portId
	 * @param field
	 * @return
	 */
	Object loadPortField(Long portId, String field);

	/**
	 * clear all reference made to the provided port id as a breaker port id
	 * @param portId
	 */
	void clearBrkrPortReference(Long portId);

	/**
	 * get all ports of a given port subclass
	 * @param portSubClass
	 * @return
	 */
	List<PowerPort> getPorts(Long portSubClass);

	/**
	 * get all ports of a given port subclass
	 * @param portSubClass
	 * @return
	 */
	List<PowerPort> getPorts(List<Long> portSubClass);

	/**
	 * run the diagnostics on the power ports
	 */
	void runPowerPortDiagnostics();

	/**
	 * get the name of the ports against the item
	 * @param itemId
	 * @return
	 */
	List<String> getPortsNameForItem(Long itemId);

	/**
	 * get num of ports for a given item
	 * @param itemId
	 * @return
	 */
	Long getNumOfPortForItem(Long itemId);
	
	/**
	 * Reset's all the power port's powerIQ id to null for given item.
	 * @param item
	 */
	void resetPIQId(Item item);
	
	/**
	 * get power port in the given item of a given port subclass and a given sort order
	 * @param itemId
	 * @param portSubClass
	 * @param sortOrder
	 * @return
	 */
	PowerPort getPort(Long itemId, Long portSubClass, int sortOrder);
	
}
