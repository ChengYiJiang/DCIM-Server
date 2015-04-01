package com.raritan.tdz.port.dao;

import com.raritan.tdz.exception.DataAccessException;

public interface SensorPortUpdaterDAO {

	/**
	 * Reset the is_sync column to false for sensor port matching the itemId
	 * and subclass   
	 *
	 * @param itemId: RackPDU or Probe item id
	 * @return
	 */
	public Integer updateSensorPortSyncToFalse(Long itemId,  Long excludeSubclass) throws DataAccessException;
	
	/**
	 * Reset the is_sync column to false for Asset strip sensor port.
	 * @param itemId
	 * @param includeSubclass
	 * @return
	 * @throws DataAccessException
	 */
	public Integer updateAssetStripSensorPortSyncToFalse(Long itemId, Long includeSubclass) throws DataAccessException;

}
