/**
 * 
 */
package com.raritan.tdz.events.home;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.events.domain.Event;

/**
 * @author prasanna
 * This interface is used by the poller to send out events to their respective 
 * implementation which handles that event.
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface EventHandler {
	
	/**
	 * A callback method to handle an event. Normally called by the EventHandlerDelegate.
	 * @param event 
	 */
	public void handleEvent(Event event);

	/**
	 * @return a name for the event handler
	 */
	public String getEventHandlerName();
	
	/**
	 * Returns the priority associated with this event handler.
	 * The priority determines what order the handler will be invoked by the delegate.
	 * @return the priority
	 */
	public int getPriority();
	
	/**
	 * A convenient set predefined set of priorities.
	 */
	public static class Priority {
		public static final int LOWEST = 0;
		public static final int LOW = 10;
		public static final int BELOW_NORMAL = 20;
		public static final int NORMAL = 30;
		public static final int ABOVE_NORMAL = 40;
		public static final int HIGH = 50;
		public static final int CRITICAL = 60;
	}
}
