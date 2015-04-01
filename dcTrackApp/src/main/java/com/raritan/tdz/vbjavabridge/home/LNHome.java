/**
 * 
 */
package com.raritan.tdz.vbjavabridge.home;

import java.util.LinkedHashMap;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber;


/**
 * @author prasanna
 * This contains the business logic for the listen-notify framework
 */
public interface LNHome {
	/**
	 * Subscribe for the notifications coming in from the Client (Windows client)
	 * @param operationLks - Event Name
	 * @param tableName - Table name associated with this event that you want to listen for
	 * @param eventHandler - An implementation of listenNotifyEventListener
	 */
	public void subscribe(LksData operationLks, String tableName,
			LNEventSubscriber eventHandler);

	/**
	 * Subscribe for the notifications coming in from the Client (Windows client)
	 * @param operationLks - Event Name
	 * @param tableName - Table name associated with this event that you want to listen for
	 * @param action - action to be performed as informed in the event
	 * @param eventHandler - An implementation of listenNotifyEventListener
	 */
	public void subscribe(LksData operationLks, String tableName, String action, 
			LNEventSubscriber eventHandler);

	/**
	 * Process the events.
	 * @param notificationName - Event Name
	 * @param tableName - Table name associated with this event that you want to listen for
	 * @param eventHandler - An implementation of listenNotifyEventListener
	 */
	public void processEvents();
	
	/**
	 * Process individual event
	 * @param lnEvent
	 */
	void processEvent(LNEvent lnEvent);

	/**
	 * Get suspend 
	 * @param suspend - Boolean suspend
	 */
	public Boolean getSuspend();
	
	/**
	 * Set suspend 
	 * @param suspend - Boolean suspend. When set to true, the processEvents will be suspended.
	 */
	public void setSuspend(Boolean suspend);

	
}
