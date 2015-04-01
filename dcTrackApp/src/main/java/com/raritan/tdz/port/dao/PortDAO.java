package com.raritan.tdz.port.dao;

import java.io.Serializable;
import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.exception.DataAccessException;

/**
 *  @author Bunty
 *  Interface to access Port domain object
 */

public interface PortDAO<T extends Serializable> extends Dao<T> {
	/**
	 * Get a data port record from the database using lazy load 
	 * @param portId - ID of port to be loaded 
	 * @return - DataPort object
	 */
	public T loadPort(Long portId) throws DataAccessException;

	/**
     * loads the evicted port
     * @param portId
     * @return
     */
	public T loadEvictedPort(Long portId);
       

	/**
	 * Check if port is used 
	 * @param portId - ID of port to check 
	 * @return - True is port is used
	 */
	boolean isPortUsed(Long portId);

	/**
	 * Get list of data ports for an item 
	 * @param itemId - ID of item  
	 * @return - List of DataPort objects
	 */
	List<T> getPortsForItem(Long itemId);

	/**
	 * Get list of data ports for a given set of sensor port Ids 
	 * @param portList - List of port Ids to be loaded 
	 * @return - List of SensorPort objects
	 */
	List<T> getPortsFromPortIdList(List<Long> portList);

	/**
	 * get the port id using the item id, port subclass and port name
	 * @param itemId
	 * @param portSubClass
	 * @param portName
	 * @return
	 */
	public Long getPortId(Long itemId, Long portSubClass, String portName);
	
	/**
	 * get the port id using the item's location, item name and port name
	 * @param location
	 * @param itemName
	 * @param portName
	 * @return
	 */
	public Long getPortId(String location, String itemName, String portName);
	
	/**
	 * get the port object using the item's location, item name and port name
	 * @param location
	 * @param itemName
	 * @param portName
	 * @return
	 */
	public T getPort(String location, String itemName, String portName);
	
	/**
	 * get the port for the item with given port subclass and port name
	 * @param itemId
	 * @param portSubClass
	 * @param portName
	 * @return
	 */
	public T getPort(Long itemId, Long portSubClass, String portName);

}
