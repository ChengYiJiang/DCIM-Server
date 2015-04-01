package com.raritan.tdz.piq.home;

import com.raritan.tdz.piq.json.Sensor.Reading;
import com.raritan.tdz.util.UnitConverterIntf;

public class PIQHumiditySensorNormalizerImpl implements	PIQSensorValueNormalizerIntf {
	
	private String sensorUnitStr; 
	private UnitConverterIntf converter;


	public String getSensorUnitStr() {
		return sensorUnitStr;
	}

	public void setSensorUnitStr(String sensorUnitStr) {
		this.sensorUnitStr = sensorUnitStr;
	}

	public UnitConverterIntf getConverter() {
		return converter;
	}

	public void setConverter(UnitConverterIntf converter) {
		this.converter = converter;
	}

	@Override
	public Double normalize(Reading r) {
		return r.getValue();
	}

	@Override
	public String getUnit() {
		return sensorUnitStr;
	}

}
