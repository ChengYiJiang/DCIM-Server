/**
 * 
 */
package com.raritan.tdz.ticket.home;


import com.raritan.tdz.domain.Item;

/**
 * @author bunty
 *
 */
public interface TicketSaveBehavior {
	
	/**
	 * update the ticketing fields with the changes in the item
	 * @param item
	 * @param additionalArgs
	 */
	public void update(Item item, Object... additionalArgs);
	
	/**
	 * update the ticketing fields with the changes in the item
	 * @param target - map of the domain object to be updated
	 * @param item
	 * @param additionalArgs
	 */
	public void update(Object target, Item item, Object... additionalArgs);

	/**
	 * update the ticket fields
	 * @param fields
	 * @param item
	 */
	void updateTicketFields(Object fields, Item item);

}
