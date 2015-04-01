package com.raritan.tdz.piq.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.json.Sensor;
import com.raritan.tdz.piq.json.SensorBase;

public class PIQSensorProcessorImpl implements PIQSensorProcessor {
	
	PIQSensorHandler sensorHandler;
	
	/* this function is called only when sensors.size() > 0 */ 
	@Override
	public void process(Item item, SensorBase sensorBase, Errors errors) throws DataAccessException{
		/* handle sensor that are not disconnected */
		if (sensorHandler.canProcess(sensorBase)) {
			sensorHandler.AddOrUpdate(item, sensorBase, errors);
		}
	}

	public PIQSensorHandler getSensorHandler() {
		return sensorHandler;
	}

	public void setSensorHandler(PIQSensorHandler sensorHandler) {
		this.sensorHandler = sensorHandler;
	}
	
}
