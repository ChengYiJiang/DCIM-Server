package com.raritan.tdz.events.service;

import java.util.Date;
import java.util.List;

import com.raritan.tdz.events.dto.EventDetailDTO;
import com.raritan.tdz.events.dto.EventSummaryDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;

/**
 * A service for managing dcTrack system events.
 * 
 * @author Andrew Cohen
 */
public interface EventService {
	
	/**
	 * Return the list of events.
	 * @return
	 * @throws ServiceLayerException
	 */
	public EventSummaryDTO getEvents() throws ServiceLayerException;

	/**
	 * Return the list of all active events.
	 * @return
	 * @throws ServiceLayerException
	 */
	public EventSummaryDTO getActiveEvents() throws ServiceLayerException;
	
	/**
	 * Returns events that have been archived but not yet purged from the database.
	 * @return
	 * @throws ServiceLayerException
	 */
	public EventSummaryDTO getClearedEvents() throws ServiceLayerException;
	
	/**
	 * Fetches the details of a particular event.
	 * @param eventId the event id
	 * @return
	 * @throws ServiceLayerException
	 */
	public List<EventDetailDTO> getEventDetails(long eventId) throws ServiceLayerException;
	
	/**
	 * Clears the specified events.
	 * @param eventIds - a list event ids
	 * @throws ServiceLayerException
	 */
	public int clearEvents(List<Integer> eventIds) throws ServiceLayerException;
	
	/**
	 * Purges the specified events. Only cleared events may be purged!
	 * @param eventIds - a list event ids
	 * @throws ServiceLayerException
	 */
	public int purgeEvents(List<Integer> eventIds) throws ServiceLayerException;
	
	/**
	 * Deletes all events that occurred before a specified date.
	 * @param beforeDate the date threshold
	 * @return the number of events deleted
	 */
	public int purgeEvents(Date beforeDate) throws ServiceLayerException;
	
	/**
	 * Get the total number of events.
	 * @return the total number of events in the event log.
	 */
	public long getEventCount() throws ServiceLayerException;
	
	/**
	 * Clears all "Active" events in the system.
	 * @return the actual number of events cleared.
	 * @throws DataAccessException
	 */
	public int clearAllEvents() throws DataAccessException;
	
	/**
	 * Purges all "Cleared" events in the system.
	 * @return the actual number of events purged.
	 * @throws DataAccessException
	 */
	public int purgeAllEvents() throws DataAccessException;
}
