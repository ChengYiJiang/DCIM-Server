/**
 * This class provides functionality to get PowerPort or 
 * related data by executing HQL functions defined in 
 * src/main/resources/mappings/PowerPortDaoFunctions.xml.
 */
package com.raritan.tdz.port.dao;

import java.util.List;

import com.raritan.tdz.domain.SensorPort;

/**
 * @author basker
 *
 */
public interface SensorPortFinderDAO {
	
	/**
	 * This function returns SensorPort whose Power IQ Id, sorOrder and type match.
	 * @param itemId : RackPdu item id. 
	 * @param piqId : PIQ id of the sensorPort
	 * @param sorOrder : sorOrder (ordinal) of the sensor port
	 * @param subclass : Sensor subClassValueCode
	 * @return List<SensorPort>
	 */
	public List<SensorPort> findSensorPortByPiqIdSubClassAndSortOrder(long itemId, long piqId, int sortOrder, long subclass);
	/**
	 * This function returns powerPort whose type and sorOrder match.
	 * @param itemId : RackPdu item id. 
	 * @param sorOrder : sorOrder (ordinal) of the sensor port
	 * @param subclass : Sensor subClassValueCode
	 * @return List<SensorPort>
	 */
	public List<SensorPort> findSensorPortByTypeAndOrder(long itemId, int sortOrder, long subclass);

	/**
	 * Get list of PDU sensor ports that are not in sync. 
	 * This function excludes the sensor with subclass
	 * @param itemId : RackPdu item id.
	 * @return List<SensorPort>
	 */
	//public List<SensorPort> findPortsNotInSync (long itemId, long subclass);
	
	/**
	 * Get list of PDU sensor ports that are in sync. 
	 * @param itemId : RackPdu item id.
	 * @return List<SensorPort>
	 */
	public List<SensorPort> findPorts(long itemId, long subclass, int sortOrder);

	/**
	 * Get list of PDU sensor ports for a given subclass
	 * @param itemId : RackPdu item id.
	 * @return List<SensorPort>
	 */
	public List<SensorPort> findPortsBySubclass (long itemId, long subclass);	
	
	/**
	 * Get list of PDU sensor ports that are not in sync. 
	 * @param itemId : RackPdu item id.
	 * @return List<SensorPort>
	 */
	//public List<SensorPort> findAssetStripPortsNotInSync (long itemId, long subclass);
	
	
	/**
	 * Gives a list of used port id given an item id
	 * @param itemId
	 * @return
	 */
	public List<Long> findUsedPorts(Long itemId);
	
}
