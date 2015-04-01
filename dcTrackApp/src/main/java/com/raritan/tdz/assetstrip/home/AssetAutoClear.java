package com.raritan.tdz.assetstrip.home;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.home.EventHandler;

/**
 * An event handler that will process asset events and determine which
 * events can be automatically cleared.
 * @author Andrew Cohen
 */
public interface AssetAutoClear extends EventHandler {

	/**
	 * Finds the most recent "asset tag connect" event at the same U-position
	 * as the specified "asset tag disconnect" event and clears it only if
	 * no item is present. 
	 * @param assetTagDisconnectEvt the "asset tag disconnect" event
	 */
	public void autoClearAssetTagConnectEvent(Event assetTagDisconnectEvt);
}
