package com.raritan.tdz.piq.home;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.raritan.tdz.assetstrip.home.AssetEventBuilder;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.EventsJSON;
import com.raritan.tdz.piq.json.EventsJSON.EventJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;

/**
 * REST client implementation for PIQ event services using Spring RestTemplate.
 * 
 * @author Andrew Cohen
 */
public class PIQEventClientImpl extends PIQRestClientBase implements PIQEventClient {
	
	/** Date format used to query for events created after a specified date */
	private static final String EVENT_QUERY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
	
	private EventHome eventHome;
	
	public PIQEventClientImpl(ApplicationSettings appSettings, EventHome eventHome) throws DataAccessException {
		super( appSettings );
		this.eventHome = eventHome;
	}
	
	@Override
	public List<PIQEvent> getAllEvents() {
		try {
			return searchEvents( null );
		}
		catch (RemoteDataAccessException e) {
			// Just return an empty list - we've already logged the error.
			return new LinkedList<PIQEvent>();
		}
	}

	@Override
	public List<PIQEvent> getAssetEvents() throws RemoteDataAccessException {
		List<PIQEvent> events = new LinkedList<PIQEvent>();
		Date eventQueryDate = null;
		
		try {
			eventQueryDate = getEventQueryDate();
		}
		catch (DataAccessException e) {
			log.error("Error getting latest event date from dcTrack event log", e);
			return events;
		}
		
		// Search for just asset event types
		int[] assetEventTypes = new int[] { 
				PIQEvent.ASSET_TAG_CONNECTED,
				PIQEvent.ASSET_TAG_REMOVED,
				PIQEvent.ASSET_STRIP_CONNECTED,
				PIQEvent.ASSET_STRIP_REMOVED
		};
		
		events.addAll( searchEvents(eventQueryDate, assetEventTypes) );
		
		Collections.sort( events );
		
		log.info("Fetched " + events.size() + " PIQ asset events");
		
		return events;
	}
	
	@Override
	public void clearEvents(List<PIQEvent> piqEvents) throws RemoteDataAccessException {
		if (piqEvents == null || piqEvents.isEmpty()) {
			return;
		}
		
		// Build the request body
		Map<String, Object> body = new HashMap<String, Object>(1);
		List<Map<String, Object>> events = new LinkedList<Map<String,Object>>();
		for (PIQEvent piqEvent : piqEvents) {
			Map<String, Object> event = new HashMap<String, Object>(1);
			event.put("id", piqEvent.getEventId());
			events.add( event );
		}
		body.put("events", events);
		
		// Make the rest call to clear the events
		doRestPost( body, "clear_batch" );
	}
	
	@Override
	public void updateEventQueryDate() throws DataAccessException {
		Date date = getLatestEventDate();
		if (date != null) {
			appSettings.setDateProperty(Name.PIQ_EVENT_QUERY_DATE, date);
		}
	}
	
	//
	// Private helper methods
	//
	
	private Date getEventQueryDate() throws DataAccessException {
		Date date = appSettings.getDateProperty( Name.PIQ_EVENT_QUERY_DATE );
		
		if (date == null) {
			// This should only be the case when running the
			// first time with the new event filtering code
			log.info("No previous PIQ event query date stored - will fetch most recent event date");
			date = getLatestEventDate();
		}
		
		return date;
	}
	
	private Date getLatestEventDate() throws DataAccessException {
		Date date = eventHome.getLatestEventDate( AssetEventBuilder.EVENT_SOURCE );
		
		if (date != null) {
			// Add one second to the time of the last event.
			// This is a workaround because PIQ always returns the most recent event that we already processed!
			// CR #39542
			Calendar cal = Calendar.getInstance();
			cal.setTime( date );
			cal.add(Calendar.SECOND, 1);
			date = cal.getTime();
		}
		
		return date;
	}
	
	/**
	 * Search for events of a particular type occurring after a specified date.
	 * @param fromDate
	 * @param eventConfigId
	 * @return a list of events
	 * @throws RemoteDataAccessException
	 */
	private List<PIQEvent> searchEvents(Date fromDate, int[] eventConfigIds) throws RemoteDataAccessException {
		SimpleDateFormat sdf = new SimpleDateFormat( EVENT_QUERY_DATE_FORMAT );
		
		// Build the event query URL parameters
		boolean first = true;
		StringBuffer urlParams = new StringBuffer("?");
		for (int eventConfigId : eventConfigIds) {
			if (!first) {
				urlParams.append("&");
			}
			else {
				first = false;
			}
			urlParams.append("event_config_id_in[]=").append( eventConfigId );
		}
		
		
		if (fromDate != null) {
			urlParams.append("&created_at_greater_than=");
			urlParams.append( sdf.format(fromDate) );
		}
		
		return searchEvents( urlParams.toString() );
	}
	
	/**
	 * Search for events with the given query string.
	 * @param queryString url GET parameters to send on the events query.
	 * @return a list of events
	 * @throws RemoteDataAccessException
	 */
	private List<PIQEvent> searchEvents(String queryString) throws RemoteDataAccessException {
		List<PIQEvent> events = new LinkedList<PIQEvent>();
	
		ResponseEntity<?> restResult = doRestGet( queryString, EventsJSON.class );
		if (restResult != null) {
			EventsJSON eventResp = (EventsJSON)restResult.getBody();
			if (eventResp != null) {
				List<EventJSON> eventsJSON = eventResp.getEvents();
				if (eventsJSON != null) {
					events.addAll( eventsJSON );
				}
			}
		}
		
		return events;
	}
}
