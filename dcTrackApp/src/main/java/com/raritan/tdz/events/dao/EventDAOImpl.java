package com.raritan.tdz.events.dao;

import java.sql.Timestamp;
import java.util.Calendar;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;

@Transactional
public class EventDAOImpl extends DaoImpl<Event> implements EventDAO {

	String DCTRACK_EVENT_SOURCE = "dcTrack";
	String EV_PARAM_STATUS = "Status";
	String EV_PARAM_ITEMNAME = "Item Name";
	String EV_PARAM_NUM_OF_ERRORS = "Errors";
	String EV_PARAM_NUM_OF_WARNINGS = "Warnings";
	String EV_PARAM_NUM_OF_INFORMATION = "Information";

	private void generatePowerChainMigrationEvent(String eventSummary, EventSeverity severity, String status, String itemName) {

		Session session = this.getNewSession(); //this.getSession();
		
		try {
			Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
			Event ev = Event.createEvent(session, createdAt, Event.EventType.MIGRATION, DCTRACK_EVENT_SOURCE);
			ev.setSeverity(session, severity);
			ev.setSummary(eventSummary);
			if (null != status) {
				ev.addParam(EV_PARAM_STATUS, status);
			}
			if (null != itemName) {
				ev.addParam(EV_PARAM_ITEMNAME, itemName);
			}
			session.save(ev);
			session.flush();
			
		}
		finally {
		
			session.close();
		}
	}
	
	@Override
	public void generatePowerChainMigrationStartEvent(String purpose) {
		
		String evtSummary = "Power Chain Migration started";
		if (purpose != null && purpose.equals("import")) {
			evtSummary = "Power Chain update for imported circuits started";
		}
		
		generatePowerChainMigrationEvent(evtSummary, EventSeverity.INFORMATIONAL, null, null);
	}

	@Override
	public void generatePowerChainMigrationEndEvent(String status, String purpose) {
		
		String evtSummary = "Power Chain Migration finished";
		if (purpose != null && purpose.equals("import")) {
			evtSummary = "Power Chain update for imported circuits finished";
		}
		
		generatePowerChainMigrationEvent(evtSummary, EventSeverity.INFORMATIONAL, status, null);

	}

	
	private void generatePowerChainDiagonsticsEvent(String eventSummary, EventSeverity severity, Long numOfErrors, Long numOfWarnings, Long numOfInformations) {

		Session session = this.getNewSession(); //this.getSession();
		
		try {
			Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
			Event ev = Event.createEvent(session, createdAt, Event.EventType.MIGRATION, DCTRACK_EVENT_SOURCE);
			ev.setSeverity(session, severity);
			ev.setSummary(eventSummary);
			if (null != numOfErrors) {
				ev.addParam(EV_PARAM_NUM_OF_ERRORS, numOfErrors.toString());
			}
			if (null != numOfWarnings) {
				ev.addParam(EV_PARAM_NUM_OF_WARNINGS, numOfWarnings.toString());
			}
			if (null != numOfInformations) {
				ev.addParam(EV_PARAM_NUM_OF_INFORMATION, numOfInformations.toString());
			}
			session.save(ev);
			session.flush();
			
		}
		finally {
		
			session.close();
		}
	}

	@Override
	public void generatePowerChainEvent(String message, String itemName) {
		
		generatePowerChainMigrationEvent(message, EventSeverity.WARNING, null, itemName);
	}
	
	@Override
	public void generatePowerChainDiagnosticsEvent(String diagnosticsType, Long numOfErrors, Long numOfWarnings, Long numOfInformations) {
		
		StringBuffer msgBuf = new StringBuffer()
		.append(diagnosticsType)
		.append(" completed");
		
		EventSeverity severity = EventSeverity.INFORMATIONAL;
		
		if (numOfErrors > 0) {
			severity = EventSeverity.CRITICAL;
		}
		else if (numOfWarnings > 0) {
			severity = EventSeverity.WARNING;
		}
		
		generatePowerChainDiagonsticsEvent(msgBuf.toString(), severity, numOfErrors, numOfWarnings, numOfInformations);
	}

}
