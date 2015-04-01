package com.raritan.tdz.piq.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.AssetStrip;
import com.raritan.tdz.piq.json.Sensor;
import com.raritan.tdz.piq.json.SensorBase;
import com.raritan.tdz.util.UnitConverterLookup;

public class PIQSensorUtilImpl implements PIQSensorUtil{
	
	@Autowired
	private SensorEventsHelper sensorEventsHelper;

	@Autowired
	private MessageSource messageSource;

	@Override
	public String makeNameUnique(String label) {
		int startIndex = label.lastIndexOf('(');
		int endIndex = label.lastIndexOf(')');
		String subName = label; 
		int num = 1;

		if (startIndex != -1 && endIndex != -1) {
			subName = label.substring(0, startIndex);
			String numStr = label.substring(startIndex + 1, endIndex);
			if (numStr != null && numStr.length() > 0) {
				try {
					num = Integer.parseInt(numStr);
					num ++;
				} catch (NumberFormatException nfe) {
					subName = label;
					num = 1;
				}
			}
		}
		StringBuffer sb = new StringBuffer(subName);
		sb.append("(");
		sb.append(num);
		sb.append(")");
		label = sb.toString();
		return sb.toString();
	}

	@Override
	public void correctSensorNames(Item item, List<Sensor> sensors,
			Errors errors) throws DataAccessException {

		List<Sensor> correctedList = new ArrayList<Sensor>();
		
		for (Sensor s: sensors) {
			// getRemoved will report information about removed status.
			// If sensor is not removed it will be null.
			if (s != null && s.getRemoved() == null) {
				String sensorNameFromPIQ = s.getLabel();
				boolean fixed = fixSensorName(item, correctedList, s, errors);
	
				if (fixed == true) {
					/* port name changed, report event */
					String code = "piqSync.sensorNameDuplicate";
					Object[] args = { item.getItemName(), sensorNameFromPIQ, s.getOrdinal(), s.getLabel() };
					errors.rejectValue("SyncSensor", code, args, "Duplicate port name");
					String evtSummary = messageSource.getMessage(code, args, Locale.getDefault());
						
					sensorEventsHelper.AddSensorEvent(item.getItemName(), s.getLabel(), sensorNameFromPIQ, s.getOrdinal(), EventType.SENSOR_UPDATE, EventSeverity.WARNING, evtSummary, null);
				}
			}
		}		
	}
	
	@Override
	public void isSorOrderDistinct(Item item, List<Sensor> sensors, Errors errors) throws DataAccessException {
		Map<Long, List<Sensor>> sensorsByClassMap = groupSensorsByClass(sensors);
		for (Map.Entry<Long, List<Sensor>> entry: sensorsByClassMap.entrySet()) {
			validate( item.getItemName(), entry.getKey(), entry.getValue(), errors);
		}
	}
	
	@Override
	public void removeSensors(Item item, List<SensorPort> sensorsForDeletion) {
		for (SensorPort sp :sensorsForDeletion ) {
			if (sp != null) item.getSensorPorts().remove(sp);
		}
	}

	@Override
	public void deleteUnsyncSensorPortsEvent(Item item, SensorPort sp,  Errors errors) throws DataAccessException {
		
		String code = "piqSync.sensorDelete";
		String itemName = item.getItemName();
		Object[] args = { itemName, sp.getPortName(), sp.getSortOrder() };
		errors.rejectValue("Sensor", code, args, "sensor delete");
		String evtSummary = messageSource.getMessage(code, args, Locale.getDefault());

		// add sensor delete event
		sensorEventsHelper.AddDeleteSensorEvent(itemName, sp, evtSummary);
	}
	
	//<--------private methods ------->//
	
	private boolean fixSensorName(Item item, List<Sensor> correctedList, Sensor s, Errors errors) {
		boolean fixed = false;
		if (correctedList.size() == 0) {
			correctedList.add(s);
		}
		else {
			for (Sensor cs: correctedList) {
				if (cs.getLabel().equals(s.getLabel()) && cs.getOrdinal() != s.getOrdinal()) {
					String uniqueName = makeNameUnique(s.getLabel());
					s.setLabel(uniqueName);
					fixSensorName(item, correctedList, s, errors);
					fixed = true;
					break;
				}
			}
			correctedList.add(s);
				
		}
		return fixed;
	}
	
	private Map<Long, List<Sensor>> groupSensorsByClass(List<Sensor> sensors) {
		Map<Long, List<Sensor>> sensorsByClassMap = new HashMap<Long, List<Sensor>>();
		
		for (Sensor s: sensors) {
			String attributeName = (s.getAttributeName() != null) ? s.getAttributeName() : "ASSET_STRIP"; /* PIQ does not provide attribute name for asset strips */
			Long key = UnitConverterLookup.sensorSubClass.get(attributeName);
			if (key != null) {
				if (sensorsByClassMap.get(key)!= null) {
					sensorsByClassMap.get(key).add(s);
				} else {
					List<Sensor> sensorListBytype = new ArrayList<Sensor>();
					sensorListBytype.add(s);
					sensorsByClassMap.put(key, sensorListBytype);
				}
			}
		}
		return sensorsByClassMap;
	}
	
	private void validate(String itemName, Long subclass, List<Sensor> sensors,  Errors errors) throws DataAccessException {
		List<Integer> sortOrderlist = new ArrayList<Integer>();
		for (Sensor s: sensors) {
			SensorBase sb = s;
			if (s != null  && s.getRemoved() == null) {
				Integer ordinal = s.getOrdinal();
				if (sortOrderlist.contains(ordinal) ) {
					// set error		
					String sensorName = (subclass == SystemLookup.PortSubClass.ASSET_STRIP) ? ((AssetStrip)sb).getName() : ((Sensor)sb).getLabel();
					setError(itemName, subclass, sensorName, ordinal, errors);
				} else {
					sortOrderlist.add(ordinal);
				}
			}
		}
	}
	
	private void setError(String itemName, Long subclass, String sensorName, Integer ordinal, Errors errors) throws DataAccessException {
		String code = "piqSync.sorOrderNotUnique";
		Object[] args = { itemName, sensorName, ordinal };
		errors.rejectValue("SyncSensor", code, args, "Port sortOrder is not distinct");
		String evtSummary = messageSource.getMessage(code, args, Locale.getDefault());
		sensorEventsHelper.AddSensorEvent(itemName, sensorName, null, ordinal, EventType.SENSOR_UPDATE, EventSeverity.CRITICAL, evtSummary, null);		
	}
	
}
