package com.raritan.tdz.events.home;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.context.support.ResourceBundleMessageSource;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventStatus;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.DataAccessException;

/**
 * Business layer for managing dcTrack system events.
 * 
 * @author Andrew Cohen
 */
public interface EventHome {

	/**
	 * Create and persist a new event.
	 * @param type the event type
	 * @param severity the event severity
	 * @param source source of this event
	 * @return
	 * @throws DataAccessException
	 */
	public Event createEvent(EventType type, EventSeverity severity, String source) throws DataAccessException;
	
	/**
	 * Create and persist a new event with a specified created at time.
	 * @param type the event type
	 * @param severity the event severity
	 * @param source source of this event
	 * @param createdAt the event time
	 * @return
	 * @throws DataAccessException
	 */
	public Event createEvent(Timestamp createdAt, EventType type, EventSeverity severity, String source) throws DataAccessException;
	
	/**
	 * Saves or updates an event.
	 * @param event the event
	 * @throws DataAccessException
	 */
	public void saveEvent(Event event) throws DataAccessException;
	
	/**
	 * Change the severity of an event.
	 * @param event the event
	 * @param severity the severity
	 * @throws DataAccessException
	 */
	public void setSeverity(Event event, EventSeverity severity) throws DataAccessException;
	
	/**
	 * Change the type of event. 
	 * @param event
	 * @param type
	 * @throws DataAccessyxception
	 */
	public void setEventType(Event event, EventType type) throws DataAccessException;
	
	/**
	 * @return a summary of events.
	 * @throws DataAccessException
	 */
	public EventSummary getEvents() throws DataAccessException;
	
	/**
	 * @return a summary of all active events.
	 * @throws DataAccessException
	 */
	public EventSummary getActiveEvents() throws DataAccessException;
	
	/**
	 * @return a summary of all cleared events.
	 * @throws DataAccessException
	 */
	public EventSummary getClearedEvents() throws DataAccessException;
	
	/**
	 * Returns a filtered list of events for a particular event type and,
	 * optionally, a matching event parameter name and value.
	 * @param type the event type
	 * @param paramName the parameter name. If null ignores parameters in the filter.
	 * @param paramValue the parameter. If null ignores parameters in the filter.
	 * @return
	 */
	public List<Event> filterEvents(EventType type, String paramName, String paramValue) throws DataAccessException;
	
	/**
	 * Returns a filtered list of active events for a particular event type and,
	 * optionally, a matching event parameter name and value.
	 * @param type the event type
	 * @param paramName the parameter name. If null ignores parameters in the filter.
	 * @param paramValue the parameter. If null ignores parameters in the filter.
	 * @return
	 */
	public List<Event> filterActiveEvents(EventType type, String paramName, String paramValue) throws DataAccessException;
	
	/**
	 * Returns a specified event with all of its parameters.
	 * @param eventId the event id
	 * @return an event with its associated parameters
	 * @throws DataAccessException
	 */
	public Event getEventDetail(long eventId) throws DataAccessException;
	
	/**
	 * Clears the list of specified events.
	 * @param eventIds a list of event Ids.
	 * @return the actual number of events cleared.
	 * @throws DataAccessException
	 */
	public int clearEvents(List<Long> eventIds) throws DataAccessException;
	
	/**
	 * Clears the list of specified events.
	 * @param eventIds a list of event Ids.
	 * @param clearing the event which is clearing the specified events.
	 * @return the actual number of events cleared.
	 * @throws DataAccessException
	 */
	public int clearEvents(List<Long> eventIds, Event clearingEvent) throws DataAccessException;
	
	/**
	 * Clears all "Active" events in the system.
	 * @param eventIds a list of event Ids.
	 * @param clearing the event which is clearing the specified events.
	 * @return the actual number of events cleared.
	 * @throws DataAccessException
	 */
	public int clearAllEvents() throws DataAccessException;
	
	/**
	 * Purge a list of events from the database.
	 * @param eventIds the list of event IDs
	 * @return the actual number of events purged.
	 * @throws DataAccessException
	 */
	public int purgeEvents(List<Long> eventIds) throws DataAccessException;
	
	/**
	 * Purges all events that occurred on or before a specified date.
	 * @param beforeDate the date threshold
	 * @return the actual number of events purged.
	 * @throws DataAccessException
	 */
	public int purgeEvents(Date beforeDate) throws DataAccessException;
	
	
	/**
	 * Purges all "Cleared" events in the system.
	 * @return the actual number of events purged.
	 * @throws DataAccessException
	 */
	public int purgeAllEvents() throws DataAccessException;
	
	/**
	 * Get the total number of events.
	 * @return the total number of events in the event log.
	 */
	public long getEventCount() throws DataAccessException;
	
	/**
	 * Returns the time of the most recent event with the given event source.
	 * @param eventDate if null, returns most recent event regardless of the event source
	 * @return
	 */
	public Date getLatestEventDate(String eventDate) throws DataAccessException;
	
	/**
	 * @return a message source for system event log messages.
	 */
	public ResourceBundleMessageSource getMessageSource(); 
	
	/**
	 * Adds item information as event parameters. This include item name, cabinet,
	 * and location.
	 * @param ev the event to add the item parameters to
	 * @param item the item
	 */
	public void addItemEventParams(Event ev, Item item);
	
	/**
	 * Retrieves the most recent event with the given criteria.
	 * @param type optional event type
	 * @param status optional event status
	 * @param severity optional event severity
	 * @param summaryContains optional text that event summary should contain
	 * @return
	 */
	public Event getMostRecentEvent(EventType type, EventStatus status, EventSeverity severity, String summaryContains) throws DataAccessException;

	/**
	 * Returns a filtered list of cleared events for a particular event type and,
	 * optionally, a matching event parameter name and value.
	 * @param type the event type
	 * @param paramName the parameter name. If null ignores parameters in the filter.
	 * @param paramValue the parameter. If null ignores parameters in the filter.
	 * @return
	 */
	public List<Event> filterClearedEvents(EventType type, String paramName,
			String paramValue) throws DataAccessException;
}
