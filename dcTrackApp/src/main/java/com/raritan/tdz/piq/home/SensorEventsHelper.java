package com.raritan.tdz.piq.home;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.json.Sensor;

public interface SensorEventsHelper {
	
	public void AddSensorEvent(String itemName, String sensorName, String oldName, Integer sortOrder, EventType type, EventSeverity severity, String summary, String uom) throws DataAccessException;
	
	public Event AddSensorEvent(String itemName, String sensorName, Integer sortOrder, EventType type, EventSeverity severity, String summary) throws DataAccessException;
	
	public void AddInvalidSensorEvent(Item item, Sensor s, String evtSummary) throws DataAccessException;
	
	public void AddDeleteSensorEvent(String itemName, SensorPort sp, String evtSummary) throws DataAccessException;

}
