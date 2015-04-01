package com.raritan.tdz.assetstrip.home;

import java.util.List;

import org.apache.log4j.Logger;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.domain.EventParam;
import com.raritan.tdz.events.home.EventHandlerDelegate;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * Asset Auto clear default implementation.
 * @author Andrew Cohen
 */
public class AssetAutoClearImpl implements AssetAutoClear {

	private Logger log = Logger.getLogger( this.getClass() );
	private EventHome eventHome;
	
	public AssetAutoClearImpl(EventHome eventHome, EventHandlerDelegate eventHandlerDelegate) {
		this.eventHome = eventHome;
		eventHandlerDelegate.registerEventHandler( this );
	}
	
	@Override
	public void handleEvent(Event event) {
		long eventCode = event.getType().getLkpValueCode();
		if (eventCode == SystemLookup.EventType.ASSET_TAG_REMOVED) {
			autoClearAssetTagConnectEvent( event );
		}
		else if (eventCode == SystemLookup.EventType.ASSET_STRIP_CONNECTED) {
			autoClearAssetStripDisconnectEvent( event );
		}
	}

	@Override
	public void autoClearAssetTagConnectEvent(Event assetTagDisconnectEvt) {
		if (assetTagDisconnectEvt.getItem() != null) {
			return;
		}
		
		// Get the U-position for the asset tag remove event
		String paramName = AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString();
		EventParam uPosParam = assetTagDisconnectEvt.getEventParams().get( paramName );
		if (uPosParam == null) {
			log.warn("No U-position found for asset tag disconnect event! " + assetTagDisconnectEvt);
			return;
		}
		
		String uPos = uPosParam.getValue();
		
		if (uPos != null) {
			List<Event> events = null;
			try {
				// Search for asset tag connect events at that same  U-position
				events = eventHome.filterActiveEvents(EventType.ASSET_TAG_CONNECTED, paramName, uPos);
			} 
			catch (DataAccessException e) {
				log.error("Error searching for matching asset tag connect event!", e);
				events = null;
			}
			
			if (events != null && !events.isEmpty()) {
				Event event = events.get(0); // Get most recent event if more than one
				if (event.getItem() == null) {
					event.clear( assetTagDisconnectEvt );
					assetTagDisconnectEvt.clear( null );
				}
			}
		}
	}
	
	//
	// EventHandler Implementation
	//
	@Override
	public String getEventHandlerName() {
		return "AssetAutoClear";
	}
	@Override
	public int getPriority() {
		// Return LOW priority so that other Auto-Association handlers are invoked before auto clearing.
		return Priority.LOW;
	}
	
	//
	// Private methods
	//
	
	private void autoClearAssetStripDisconnectEvent(Event assetTagConnectEvt) {
		List<Event> events = null;
		
		String paramName = AssetEventParam.ASSET_STRIP_ID.toString();
		EventParam param = assetTagConnectEvt.getEventParams().get( paramName );
		if (param == null) {
			log.warn("No asset strip ID parameter on event! " + assetTagConnectEvt);
			return;
		}
		
		String assetStripId = param.getValue();
		
		try {
			// Search for asset strip events for the same ID
			events = eventHome.filterEvents(EventType.ASSET_STRIP_REMOVED, paramName, assetStripId );
		} 
		catch (DataAccessException e) {
			log.error("Error searching for matching asset tag connect event!", e);
			events = null;
		}
		
		if (events != null && !events.isEmpty()) {
			Event event = events.get(0); // Get most recent event if more than one
			event.clear( assetTagConnectEvt );
			//assetTagConnectEvt.clear( null );
		}
	}
}
