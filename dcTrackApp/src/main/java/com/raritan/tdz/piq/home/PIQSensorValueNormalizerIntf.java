package com.raritan.tdz.piq.home;

import com.raritan.tdz.piq.json.Sensor.Reading;

public interface PIQSensorValueNormalizerIntf {
	
	public Double normalize (Reading r);
	
	public String getUnit(); 

}
