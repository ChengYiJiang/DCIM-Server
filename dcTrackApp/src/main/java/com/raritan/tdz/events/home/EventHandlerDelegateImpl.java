package com.raritan.tdz.events.home;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.raritan.tdz.events.domain.Event;

/**
 * Event Handler Delegate implementation.
 * NOTE: This implementation is currently single threaded, but, if we later need to optimize, we can rework the internals to multi-thread based on the priority on EventHandlers.

 * @author Andrew Cohen
 */
public class EventHandlerDelegateImpl implements EventHandlerDelegate {

	private final Logger log = Logger.getLogger( EventHandlerDelegate.class );
	
	/** Internal map of event handler services where the key is the EventHandler priority */
	private Map<Integer, EventHandlerService> eventHandlers;
	
	public EventHandlerDelegateImpl() {
		eventHandlers = new TreeMap<Integer, EventHandlerService>( new Comparator<Integer>() {
			@Override
			public int compare(Integer priority1, Integer priority2) {
				return priority2.compareTo( priority1 );
			}
		});
	}
	
	@Override
	public synchronized void registerEventHandler(EventHandler eventHandler) {
		int priority = eventHandler.getPriority();
		EventHandlerService service = eventHandlers.get( priority );
		if (service == null) {
			service = new EventHandlerService( priority );
			eventHandlers.put(priority, service);
		}
		service.addHandler( eventHandler );
	}

	@Override
	public synchronized void unregisterEventHandler(EventHandler eventHandler) {
		int priority = eventHandler.getPriority();
		EventHandlerService service = eventHandlers.get( priority );
		if (service != null) {
			service.removeHandler( eventHandler );
			if (service.eventHandlers.isEmpty()) {
				eventHandlers.remove( priority );
			}
		}
	}
	
	@Override
	public void processEvent(Event event) {
		for (Integer priority : eventHandlers.keySet()) {
			log.info("priority: " + priority);
			EventHandlerService service = eventHandlers.get( priority );
			service.processEvent( event );
		}
	}

	
	/**
	 * A service that delegates the event to all EventHandlers with a specified priority.
	 */
	private class EventHandlerService {
		private int priority;
		private Set<EventHandler> eventHandlers;
		
		public EventHandlerService(int priority) {
			this.priority = priority;
			eventHandlers = new HashSet<EventHandler>();
		}
		
		public void processEvent(Event event) {
			for (EventHandler eventHandler : eventHandlers) {
				if (log.isDebugEnabled()) {
					log.debug( "Invoking Event Handler: " + eventHandler.getEventHandlerName() );
				}
				eventHandler.handleEvent( event );
			}
		}
		
		public void addHandler(EventHandler eventHandler) {
			eventHandlers.add( eventHandler );
		}
		
		public boolean removeHandler(EventHandler eventHandler) {
			return eventHandlers.remove( eventHandler );
		}
		
		public int getPriority() {
			return priority;
		}
	}
}
