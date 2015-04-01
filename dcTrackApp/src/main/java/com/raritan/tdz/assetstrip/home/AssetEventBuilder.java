package com.raritan.tdz.assetstrip.home;

import java.util.List;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.home.PIQEvent;

/**
 * A service which converts raw PIQ asset events into dcTrack events and
 * stores them in the database.
 * 
 * @author Andrew Cohen
 */
public interface AssetEventBuilder {

	// dcTrack event source for asset events
	public static final String EVENT_SOURCE = "Asset Strip";
	
	/**
	 * Create new persistent dcTrack asset tag events from PIQ events.
	 * @param event
	 * @return a list of event Ids
	 * @throws DataAccessException
	 */
	public List<Event> buildAssetEvents(List<PIQEvent> event);
}
