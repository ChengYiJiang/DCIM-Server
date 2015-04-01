package com.raritan.tdz.events.dto;

import java.util.List;

/**
 * Event Summary DTO that will be exposed via BlazeDS.
 * 
 * @author Andrew Cohen
 */
public interface EventSummaryDTO {

	/**
	 * Returns all events.
	 * @return
	 */
	public long getGrandTotal();

	/**
	 * Returns the list of events matching the client.
	 * @return
	 */
	public List<EventDTOImpl> getEvents();

	public void setGrandTotal(long grandTotal);

	public void setEvents(List<EventDTOImpl> events);
}