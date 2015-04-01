/**
 * 
 */
package com.raritan.tdz.port.dao;

import java.util.List;

/**
 * @author prasanna
 *
 */
public interface DataPortFinderDAO {

	/**
	 * Gives a list of used port id given an item id
	 * @param itemId
	 * @return
	 */
	public List<Long> findUsedPorts(Long itemId);
	
}
