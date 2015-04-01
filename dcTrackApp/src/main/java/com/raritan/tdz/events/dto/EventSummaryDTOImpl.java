package com.raritan.tdz.events.dto;

import java.util.LinkedList;
import java.util.List;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.home.EventSummary;


public class EventSummaryDTOImpl implements EventSummaryDTO {
	private EventSummary summary;
	
	public EventSummaryDTOImpl(EventSummary summary) {
		this.summary = summary;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventSummaryDTO#getGrandTotal()
	 */
	@Override
	public long getGrandTotal() {
		return summary.getGrandTotal();
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventSummaryDTO#getEvents()
	 */
	@Override
	public List<EventDTOImpl> getEvents() {
		List<EventDTOImpl> eventList = summary.getEvents(); 
		return eventList;
		
		/*List<EventListDTO> events = summary.getEvents();
		List<EventDTOImpl> eventList = new LinkedList<EventDTOImpl>();
		
		if (events != null) {
			for (EventListDTO event : events) {
				eventList.add( new EventDTOImpl(event) );
			}
		}
		
		return eventList;*/
	}
	
	// Included these "noop" setters for BlazeDS
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventSummaryDTO#setGrandTotal(long)
	 */
	@Override
	public void setGrandTotal(long grandTotal) { }
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventSummaryDTO#setEvents(java.util.List)
	 */
	@Override
	public void setEvents(List<EventDTOImpl> events) { }
}
