package com.raritan.tdz.piq.home;

import java.sql.Timestamp;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.json.Sensor;

public class SensorEventsHelperImpl implements SensorEventsHelper {
	
	@Autowired
	private EventHome eventHome;

	@Override
	public void AddSensorEvent(String itemName, String sensorName,
			String oldName, Integer sortOrder, EventType type,
			EventSeverity severity, String summary, String uom)
			throws DataAccessException {

		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Event ev = eventHome.createEvent(createdAt, type, severity, "dcTrack");
		ev.setSummary(summary);
		ev.addParam("Item Name", itemName);
		if (oldName != null && !sensorName.equals(oldName)) {
			ev.addParam("Old Name", oldName);
		}
		ev.addParam("New Name", sensorName);
		ev.addParam("Index Number", sortOrder.toString());
		if (uom != null && uom.length() > 0) {
			ev.addParam("UOM", uom);
		}
		eventHome.saveEvent(ev);
	}

	@Override
	public Event AddSensorEvent(String itemName, String sensorName,
			Integer sortOrder, EventType type, EventSeverity severity,
			String summary) throws DataAccessException {
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Event ev = eventHome.createEvent(createdAt, type, severity, "dcTrack");
		ev.setSummary(summary);
		ev.addParam("Item Name", itemName);
		ev.addParam("Sensor Name", sensorName);
		ev.addParam("Index Number", sortOrder.toString());
		return ev;
	}

	@Override
	public void AddInvalidSensorEvent(Item item, Sensor s, String evtSummary)
			throws DataAccessException {
		Event ev = AddSensorEvent(item.getItemName(), s.getLabel(), s.getOrdinal(), EventType.INVALID_SENSOR_RESPONSE, EventSeverity.CRITICAL, evtSummary);
		ev.addParam("Type", s.getAttributeName());
		eventHome.saveEvent(ev);
	}

	@Override
	public void AddDeleteSensorEvent(String itemName, SensorPort sp,
			String evtSummary) throws DataAccessException {
		Event ev = AddSensorEvent(itemName, sp.getPortName(), sp.getSortOrder(), EventType.SENSOR_DELETE, EventSeverity.CRITICAL, evtSummary);
		String cabinetName = sp.getCabinetItem() != null ? sp.getCabinetItem().getItemName() : "" ;
		String xyzLocation = sp.getXyzLocation() != null ? sp.getXyzLocation() :  "";
		
		if (cabinetName.length() > 0) {
			ev.addParam("Position", cabinetName);
		} else if (xyzLocation.length() > 0) {
			ev.addParam("Position", xyzLocation);
		}
		eventHome.saveEvent(ev);
	}

}

