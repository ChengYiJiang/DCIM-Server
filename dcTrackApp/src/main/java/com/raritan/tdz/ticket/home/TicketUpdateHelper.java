package com.raritan.tdz.ticket.home;

import com.raritan.tdz.domain.Item;

public interface TicketUpdateHelper {

	/**
	 * helper class to implement common functionality to update the ticket fields
	 * @param behavior
	 * @param item
	 * @param additionalArgs
	 */
	void update(TicketSaveBehavior behavior, Item item, Object... additionalArgs);

	/**
	 * helper class to implement common functionality to update the ticket fields
	 * @param behavior
	 * @param target
	 * @param item
	 */
	void update(TicketSaveBehavior behavior, Object target, Item item);

}
