/**
 * 
 */
package com.raritan.tdz.port.dao;

/**
 * @author basker
 *
 */
public interface PowerPortUpdateDAO {
	
	/**
	 * Update PowerPort's outlet reading for the associated piqId.  
	 *
	 * @param reading
	 * @param piqId
	 * @param locationId TODO
	 * @return
	 */
	public int updateOutletReading(Double reading, Long piqId, Long locationId);
	
}
