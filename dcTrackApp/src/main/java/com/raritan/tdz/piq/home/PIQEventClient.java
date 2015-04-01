package com.raritan.tdz.piq.home;

import java.util.List;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;

/**
 * A REST client for dealing with PIQ events.
 * 
 * @author Andrew Cohen
 *
 */
public interface PIQEventClient extends PIQRestClient {

	/**
	 * Fetches all events from PIQ. The list is ordered by most to least recent creation date.
	 * @return
	 */
	public List<PIQEvent> getAllEvents();
	
	/**
	 * Clears the specified events in PIQ.
	 * @param events a list of events
	 */
	public void clearEvents(List<PIQEvent> events) throws RemoteDataAccessException;
	
	/**
	 * Fetches all asset strip and tag related events. The list is ordered by most to least recent creation date.
	 * @throws DataAccessException
	 * @return
	 */
	public List<PIQEvent> getAssetEvents() throws RemoteDataAccessException;
	
	/**
	 * Update the date used to query events.
	 * @throws DataAccessException
	 */
	public void updateEventQueryDate() throws DataAccessException;
}
