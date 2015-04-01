package com.raritan.tdz.port.dao;

import java.util.List;

import com.raritan.tdz.domain.DataPort;


/**
 *  @author Santo Rosario
 *  Interface to access Data Port domain object
 */

public interface DataPortDAO extends PortDAO<DataPort> {

	/**
	 * Get list of free data ports for an item  
	 * @param itemId - ID of item 
	 * @return  - List of DataPort objects
	 */	
	List<DataPort> getFreePortsForItem(Long itemId);

	/**
	 * Get a list of item name that have connected data ports given a list of item Ids 
	 * @param itemIds - List of item ids to check 
	 * @return - List of item name that were found to have one or more connected ports
	 */	
	List<String> getItemNamesWithConnectedPorts(List<Long> itemIds);

	/**
	 * delete the ip teaming and IP address. IP Address will be deleted if not shared by data port of other item
	 * @param portId
	 */
	void deletePortIPAddressAndTeaming(long portId);
	
	
	DataPort getPort(Long itemId, Long portSubClass, String portName);

	List<DataPort> getPortByName(String portName, Long itemId);

	DataPort getDataPortForItem(Long itemId, String portName);

	DataPort getFirstPort(Long itemId);

}
