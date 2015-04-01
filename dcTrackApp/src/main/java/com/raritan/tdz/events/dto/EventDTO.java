package com.raritan.tdz.events.dto;

import java.sql.Timestamp;

import com.raritan.tdz.events.domain.Event;

/**
 * An event summary view exposed via BlazeDS.
 * 
 * @author Andrew Cohen
 */
public interface EventDTO {

	public Timestamp getOccuredAt();

	public void setOccuredAt(Timestamp occuredAt);

	public String getSeverity();

	public void setSeverity(String severity);

	public String getEvent();

	public void setEvent(String event);

	/**
	 * Sets the event descriptor string based on both the event type and result status codes.
	 * @param event the event domain object
	 */
	public void setEvent(Event event);

	public long getEventId();

	public void setEventId(long eventId);

	public String getSummary();

	public void setSummary(String summary);

	public String getStatus();

	public void setStatus(String status);

	public String getSource();

	public void setSource(String source);

	public String getParams();

	public void setParams(String params);

}