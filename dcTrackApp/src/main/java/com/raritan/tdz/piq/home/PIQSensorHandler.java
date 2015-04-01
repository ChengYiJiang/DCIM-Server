package com.raritan.tdz.piq.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.piq.json.SensorBase;

public interface PIQSensorHandler {

	public boolean canProcess(SensorBase s);
	
	public void AddOrUpdate (Item item, SensorBase s, Errors errors);
	
}
