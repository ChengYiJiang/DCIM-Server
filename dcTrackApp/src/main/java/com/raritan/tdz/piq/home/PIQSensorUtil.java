package com.raritan.tdz.piq.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.json.Sensor;

public interface PIQSensorUtil {
	
	/**
	 * Make the sensor name unique by post-fixing label with '(n)'
	 * if the name already ends with (n) it increments n and make it unique.
	 * @param label
	 * @return
	 */
	public String makeNameUnique(String label);

	/**
	 * Correct sensor names that are not unique within this pdu
	 * @param item
	 * @param sensors
	 * @param errors
	 * @return 
	 * @throws DataAccessException
	 */
	public void correctSensorNames(Item item, List<Sensor> sensors, Errors errors) throws DataAccessException;

	/**
	 * checks whether the sensor ordinal is unique for a given sensor class
	 * @param item
	 * @param sensors
	 * @param errors
	 * @throws DataAccessException
	 */
	void isSorOrderDistinct(Item item, List<Sensor> sensors, Errors errors)
			throws DataAccessException;

	/**
	 * Remove sensorPort from an item that are marked for deletion
	 * @param item
	 * @param sensorsForDeletion
	 */
	void removeSensors(Item item, List<SensorPort> sensorsForDeletion);

	/**
	 * generate sensor port delete event for sensors that are out of sync (e.g. piq id of sensor does not match anymore)
	 * @param item
	 * @param sp
	 * @param errors
	 * @throws DataAccessException
	 */
	void deleteUnsyncSensorPortsEvent(Item item, SensorPort sp, Errors errors)
			throws DataAccessException;
}
