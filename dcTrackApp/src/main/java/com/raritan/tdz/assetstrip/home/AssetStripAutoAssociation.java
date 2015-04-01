/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.home.EventHandler;
import com.raritan.tdz.exception.RemoteDataAccessException;

/**
 * The service handles "Asset Strip" Connect and Remove events.
 * 
 * @author Andrew Cohen
 */
public interface AssetStripAutoAssociation extends EventHandler {
	
	/**
	 * Add Asset Strip association based on the asset tag event received from PIQ.
	 * Will attempt find the associated PDU item and cabinet from information in the event.
	 * It will also set the summary for the event.
	 * @param event - Asset Strip event
	 * @return item - Associated item
	 */
	public Item addAssociation(Event event);
	
	/**
	 * Remove Asset Strip association from the item based on the asset tag event received from PIQ
	 * @param event - Asset Strip event
	 * @return item - un-associated item
	 */
	public Item removeAssociation(Event event);
	
	/**
	 * Creates all asset strip connect and disconnect events in the system event log for the given PDU or EMX item.
	 * @param item
	 * @return
	 */
	public List<Long> createAssetStripEvents(Item item) throws RemoteDataAccessException;

	/**
	 * Creates asset strip connect events in the system event log for the given PDU or EMX item.
	 * @param item
	 * @return
	 */
	public List<Long> createAssetStripConnectedEvents(Item item) throws RemoteDataAccessException;
	
	/**
	 * Creates asset strip connect events in the system event log for the given PDU or EMX item.
	 * @param item
	 * @return
	 */
	public List<Long> createAssetStripDisconnectedEvents(Item item) throws RemoteDataAccessException;
}
