package com.raritan.tdz.piq.home;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.piq.exceptions.PIQIPAddressConflictException;
import com.raritan.tdz.piq.exceptions.PIQUpdateException;
import com.raritan.tdz.piq.json.ErrorJSON;

/**
 * Logs various system events related to PIQ synchronization.
 * @author Andrew Cohen
 */
public interface PIQSystemEventLogger {

	/**
	 * Logs a generic item error.
	 * @param error the Error JSON received from PIQ
	 * @param eventId the id of system event that was already logged for the PIQ REST response error (HTTP 4xx)
	 * @param item the item
	 * @param ipAddress the ip Address
	 * @param proxyIndex the proxy index
	 * @param eventSource TODO
	 * @return
	 */
	public Event logGenericItemError(ErrorJSON error, Long eventId, Item item, String ipAddress, String proxyIndex, String eventSource);
	
	/**
	 * Logs an error for an IP address conflict.
	 * @param eventId the id of system event that was already logged for the PIQ REST response error (HTTP 4xx)
	 * @param ex the exception
	 * @param eventSource TODO
	 * @return
	 */
	public Event logIPAddressConflict(Long eventId, PIQIPAddressConflictException ex, String eventSource);
	
	/**
	 * Logs an error when the PDU in dcTrack maps to a non-existent PDU in PIQ.
	 * @param pduItem
	 * @param ipAddress
	 * @param eventSource TODO
	 * @return
	 */
	public Event logPDUDoesNotExistInPIQ(Item pduItem, String ipAddress, String eventSource);
	
	/**
	 * Logs an event for an installed PDU with no IP Address.
	 * @param pduItem
	 * @param eventSource TODO
	 */
	public Event logInstalledPDUHasNoIpAddress(Item pduItem, String eventSource);
	
	/**
	 * Adds information from a PIQUpdateException as parameters to the given event.
	 * @param ev the event
	 * @param ex the PIQ Update Exception
	 */
	public void addPIQUpdateExceptionEventParams(Event ev, PIQUpdateException ex);
	
	/**
	 * Will ignore any requests to log a warning/error event for the same item more than once. 
	 * @param context the job execution context
	 */
	public void enableDuplicateTracking();
	
	/**
	 * Clear the internal cache used for duplicate tracking.
	 */
	public void clearDuplicateTracking();

	/**
	 * Logs an error when an item in dcTrack maps to a non-existent item in PIQ.
	 * @param item
	 * @param ipAddress
	 * @param eventSource TODO
	 * @return
	 */
	public Event logItemDoesNotExistInPIQ(Item item, String ipAddress, String eventSource);
}
