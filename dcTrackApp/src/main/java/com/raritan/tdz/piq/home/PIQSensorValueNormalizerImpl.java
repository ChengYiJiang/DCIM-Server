package com.raritan.tdz.piq.home;

import com.raritan.tdz.piq.json.Sensor.Reading;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.UnitConverterLookup;

public class PIQSensorValueNormalizerImpl implements PIQSensorValueNormalizerIntf {
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
		String sensorUom = r.getUom().toUpperCase();
		String inputUnit = UnitConverterLookup.unitsMap.get(sensorUom).toString();
		return (Double)converter.normalize(r.getValue(), inputUnit);
	}

	@Override
	public String getUnit() {
		return sensorUnitStr;
	}
}
