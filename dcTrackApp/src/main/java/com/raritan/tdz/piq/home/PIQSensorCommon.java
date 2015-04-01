package com.raritan.tdz.piq.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.json.AssetStrip;
import com.raritan.tdz.piq.json.Sensor;

public interface PIQSensorCommon {
	
	/**
	 * Delete sensors that are not in sync. This function finds matching
	 * SensorPort in dcTrack based on sortOrder and sensor subclass. All sensors
	 * that do not match the discovered sensors will be deleted. This operation
	 * adds event log entry.
	 * @param item
	 * @param sensors
	 * @param errors
	 * @throws DataAccessException
	 */
	public void deleteSensorsNotInSync(Item item, List<Sensor> sensors, Errors errors) throws DataAccessException;
	
	/**
	 * This function deletes all sensors in item.
	 * @param item
	 * @param errors
	 * @throws DataAccessException
	 */
	public void deleteAllSensors(Item item, Errors errors ) throws DataAccessException;
	
	/**
	 * Delete asset strips that are not in sync. This function finds matching
	 * SensorPort in dcTrack based on sortOrder and sensor subclass. All sensors
	 * that do not match the discovered sensors will be deleted. This operation
	 * @param item
	 * @param sensors
	 * @param errors
	 * @throws DataAccessException
	 */
	public void deleteAssetStripNotInSync(Item item, List<AssetStrip> sensors, Errors errors) throws DataAccessException;
	
	/**
	 * Delete the assetstrip sensors connected to this item 
	 * @param item
	 * @param errors
	 * @throws DataAccessException
	 */
	public void deleteAllAssetStripSensors(Item item, Errors errors) throws DataAccessException;

	/**
	 * Delete the sensors excluding the subclass in the argument.
	 * @param item
	 * @param excludeSubClass
	 * @param errors
	 * @throws DataAccessException
	 */
	public void deleteAllSensors(Item item, long excludeSubClass, Errors errors)throws DataAccessException;
	

}
