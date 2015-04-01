package com.raritan.tdz.events.home;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.events.domain.Event;

/**
 * This service allows clients to register their own specific EventHandler implementations.
 * The service internally delegates processEvent() requests to each EventHandler. Events will be delegated
 * by to each EventHandler according that EventHandler's priority, so EventHandlers with the highest priority
 * may be invoked first. EventHandlers with same priority may also be executed in parallel on any particular event.
 * 
 * @author Andrew Cohen
 */
@Transactional
public interface EventHandlerDelegate {

	/**
	 * Register an Event Handler.
	 * @param eventHandler
	 */
	public void registerEventHandler( EventHandler eventHandler );
	
	/**
	 * Unregister an Event Handler.
	 * @param eventHandler
	 */
	public void unregisterEventHandler( EventHandler eventHandler );
	
	/**
	 * Process the event by delegating to all registered Event handlers.
	 * @param event an event
	 */
	public void processEvent( Event event );
}
