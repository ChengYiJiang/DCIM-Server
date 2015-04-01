package com.raritan.tdz.piq.home;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;

public class PIQSyncLocationUtil {
	
	@Autowired
	EventHome evtHome;
	
	public static String getActualId (String locationId) {
		String id = locationId;
		if (locationId != null && locationId.contains(":")) {
			String[] ids = locationId.split(":");
			id = ids[ids.length - 1];
		}
		return id;
	}

	public static String getLocationType (String locationId) {
		String id = locationId;
		if (locationId != null && locationId.contains(":")) {
			String[] ids = locationId.split(":");
			id = ids[ids.length - 2];
		}
		else if (locationId != null && locationId.length() > 0) {
			id = locationId.startsWith("Data Center") ? "DATA_CENTER" : null;
			if (null == id) id = locationId.startsWith("Floor") ? "FLOOR" : null;
			if (null == id) id = locationId.startsWith("Room") ? "ROOM" : null;
			// TODO:: Remove since dctrack do not support Aisle and Row. Add lks for Aisle, Row
			// id = locationId.startsWith("Aisle") ? "AISLE" : null; 
			// id = locationId.startsWith("Row") ? "ROW" : null;
		}
		return id;
	}
	
	public static String getActualExtkey (String locationId) {
		String id = locationId;
		if (locationId != null && locationId.contains(":")) {
			String[] ids = locationId.split(":");
			if (ids.length == 3) {
				id = ids[ids.length - 2] + ":" + ids[ids.length - 1];
			} else {
				id = ids[ids.length - 1];
			}
		}
		return id;
	}
	
	public void setLocationEvent (String msg, EventType eventType, EventSeverity severity, String eventSource) throws DataAccessException {
		Event event = evtHome.createEvent(eventType, severity, eventSource);
		MessageSource messageSource = evtHome.getMessageSource();
		event.setSummary(messageSource.getMessage("piqSync.locationNotFoundInPIQ", new Object[]{msg}, Locale.getDefault()));
		evtHome.saveEvent(event);		
	}


}
