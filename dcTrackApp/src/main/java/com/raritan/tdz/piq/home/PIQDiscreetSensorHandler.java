package com.raritan.tdz.piq.home;

import org.apache.commons.lang.WordUtils;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.piq.json.Sensor;
import com.raritan.tdz.piq.json.Sensor.State;
import com.raritan.tdz.piq.json.SensorBase;
import com.raritan.tdz.util.UnitConverterLookup;

public class PIQDiscreetSensorHandler extends PIQSensorHandlerBase implements PIQSensorHandler {

	@Override
	protected void setSensorSpecificData(SensorPort sp, Item item, SensorBase sb, Errors errors) {
		Sensor s = (Sensor)sb;

		setSensorName(sp, item, s.getLabel(), s.getAttributeName(), s.getOrdinal(), errors);
		
		// set default value
		sp.setStatusActual("N/A");
		sp.setValueActual(-1.0);
		
		State state = s.getState();
		if (state != null) {
			String sensorState = state.getState();
			if ( sensorState != null && sensorState.length() > 0) {
				sp.setStatusActual(WordUtils.capitalizeFully(sensorState));
			}
		}
	}

	@Override
	protected Long getPortSubClass(SensorBase sb) {
		Sensor s = (Sensor)sb;
		return UnitConverterLookup.sensorSubClass.get(s.getAttributeName());
	}
}
