/**
 * 
 */
package com.raritan.tdz.piq.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.json.SensorBase;

/**
 * @author basker
 *
 */
public interface PIQSensorProcessor {
	
	void process(Item item, SensorBase sensorBase, Errors errors) throws DataAccessException;

}
