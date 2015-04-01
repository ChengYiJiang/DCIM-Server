package com.raritan.tdz.piq.home;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.piq.json.Sensor;
import com.raritan.tdz.piq.json.Sensor.Reading;
import com.raritan.tdz.piq.json.SensorBase;
import com.raritan.tdz.util.UnitConverterLookup;

public class PIQNumericSensorHandler extends PIQSensorHandlerBase implements PIQSensorHandler {

	private Map<Long, PIQSensorValueNormalizerIntf> sensorValueConverter;

	public Map<Long, PIQSensorValueNormalizerIntf> getSensorValueConverter() {
		return sensorValueConverter;
	}

	public void setSensorValueConverter(
			Map<Long, PIQSensorValueNormalizerIntf> sensorValueConverter) {
		this.sensorValueConverter = sensorValueConverter;
	}

	@Override
	protected void setSensorSpecificData(SensorPort sp, Item item, SensorBase sb, Errors errors) {
		Sensor s = (Sensor)sb;
		
		setSensorName(sp, item, s.getLabel(), s.getAttributeName(), s.getOrdinal(), errors);
		
		Reading reading = s.getReading();
		if (reading != null && reading.getId() > 0) { /* assumes readingId is > 0 */
			
			Double value = reading.getValue();
			if (value == null) {
				setDefaultValue(sp);
			} else {
				setSensorReading (sp, reading, s.getAttributeName(), item.getItemName(), errors);
			}
		} else {
			// set default value
			setDefaultValue(sp);
		}
	}
	
	private void setSensorReading (SensorPort sp, Reading reading, String attributeName, String itemName, Errors errors) {
		
		sp.setValueActual(reading.getValue());
		sp.setStatusActual(null);
		sp.setValueActualUnit(null);

		String sensorUom = reading.getUom();
		if (sensorUom != null && !sensorUom.isEmpty()) { 
			List<String> supportedUnits = UnitConverterLookup.supportedUnit.get(attributeName);
			if (supportedUnits != null && supportedUnits.contains(sensorUom.toUpperCase())) {
				Long sensorSubClass = UnitConverterLookup.sensorSubClass.get(attributeName);
				if (sensorSubClass != null) {
					PIQSensorValueNormalizerIntf sensorConverter = (PIQSensorValueNormalizerIntf)sensorValueConverter.get(sensorSubClass);
					if (sensorConverter != null) {
						Double convertedValue = sensorConverter.normalize(reading);
						sp.setValueActual(convertedValue);
						sp.setValueActualUnit(sensorConverter.getUnit());
					}
				}
			} else {
				// No units saved to db. and no conversion is applied.
				setInvalidUnitsProvidedEvent(itemName, sp, attributeName, errors);
			}
		} else {
			// Note: no unit saved hence no conversion is applied on vale
			setInvalidUnitsProvidedEvent(itemName, sp, attributeName, errors);
		}
	}
	
	private SensorPort setDefaultValue(SensorPort sp) {
		sp.setStatusActual("N/A");
		sp.setValueActual(-1.0); // TODO: -1 for numeric sensor is valid value, change this to null if CV implementation agrees.
		sp.setValueActualUnit(null);
		return sp;
	}
	
	private void setInvalidUnitsProvidedEvent(String itemName, SensorPort sp, String sensorAttribute, Errors errors) {
		String code = "piqSync.invalidUnitsProvided";
		Object[] args = { itemName, sp.getPortName(), sp.getSortOrder() };
		errors.rejectValue("portName", code, args, "port name mismatch");
		String evtSummary = messageSource.getMessage(code, args, Locale.getDefault());
		AddSensorEvent(itemName, sp.getPortName(), null, sp.getSortOrder(), EventType.SENSOR_UPDATE, EventSeverity.INFORMATIONAL, evtSummary, sensorAttribute);
	}

	protected Long getPortSubClass(SensorBase sb) {
		Sensor s = (Sensor)sb;
		return UnitConverterLookup.sensorSubClass.get(s.getAttributeName());
	}

}
