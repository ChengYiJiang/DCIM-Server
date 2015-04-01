/**
 * This class provides functionality to get PowerPort or 
 * related data by executing HQL functions defined in 
 * src/main/resources/mappings/PowerPortDaoFunctions.xml.
 */
package com.raritan.tdz.port.dao;

import java.util.List;

import com.raritan.tdz.domain.PowerPort;

/**
 * @author basker
 *
 */
public interface PowerPortFinderDAO {
	
	/**
	 * This function returns a list of powerPort where associated
	 * piqId is not null
	 * @return List<PowerPort>
	 */
	public List<PowerPort> findAssociatedPIQOutlets();
	
	/**
	 * Find PDU power port by powerIdId of port and sortOrder(ordinal)
	 * pduId: PDU id
	 * piqId: Power IQ id of the port
	 * sortOrder: sortOrder(ordinal) of the port
	 */
	public List<PowerPort> findPowerPortByPiqIdAndSortOrder(long pduId, long piqId, int sortOrder, long subclass);

	/**
	 * Find PDU power port by powerIdId of port and sortOrder(ordinal)
	 * pduId: PDU id
	 * sortOrder: sortOrder(ordinal) of the port
	 */
	public List<PowerPort> findPowerPortBySortOrder(long pduId, int sortOrder, long subclass);
	
	
	/**
	 * This function a list of powerPort piqId where associated
	 * piqId is not null
	 * @param locationId TODO
	 * @return List<Long>
	 */
	public List<Long> findPiqId(Long locationId);
	

	/**
	 * Gives a list of used port id given an item id
	 * @param itemId
	 * @return
	 */
	public List<Long> findUsedPorts(Long itemId);

	/**
	 * Find PDU power port by powerIdId of port and sortOrder(ordinal)
	 * pduId: PDU id
	 * piqId: Power IQ id of the port
	 * sortOrder: sortOrder(ordinal) of the port
	 * @param locationId TODO
	 */
	public List<PowerPort> findPowerPortByPiqId(long piqId, int sortOrder, long subclass, Long locationId);


	public List<PowerPort> findPowerPortByItemIdAndSortOrder(int itemPiqId, long locationId, int sortOrder, long subclass);
}
