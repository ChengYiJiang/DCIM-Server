/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.home.EventHandler;

/**
 * @author prasanna
 * This interface defines the methods for auto association of an asset tag to 
 * an item.
 */
public interface AssetTagAutoAssociation extends EventHandler {
	
	/**
	 * Add Asset Tag association based on the asset tag event received from PIQ
	 * @param event - Asset Tag event
	 * @return item - Associated item
	 */
	public Item addAssociation(Event event);
	
	/**
	 * Remove Asset Tag association from the item based on the asset tag event received from PIQ
	 * @param event - Asset Tag event
	 * @return item - un-associated item
	 */
	public Item removeAssociation(Event event);
}
