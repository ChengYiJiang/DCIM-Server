package com.raritan.tdz.events.home;

import java.util.List;

import com.raritan.tdz.events.dto.EventDTOImpl;

/**
 * An event summary including events matching the specified filter
 * and a grand total of all events in the system.
 * 
 * @author Andrew Cohen
 */
public class EventSummary {

	private long grandTotal = 0;
	private List<EventDTOImpl> events;
	
	EventSummary() {
	}
	
	/**
	 * Returns all events.
	 * @return
	 */
	public long getGrandTotal() {
		return grandTotal;
	}
	
	/**
	 * Returns the list fo events matching the client.
	 * @return
	 */
	public List<EventDTOImpl> getEvents() {
		return events;
	}
	
	//
	// Package protected methods for building the summary
	//
	
	void setGrandTotal(long grandTotal) {
		this.grandTotal = grandTotal;
	}
	
	void setEvents(List<EventDTOImpl> events) {
		this.events = events;
	}
}
