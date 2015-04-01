/**
 * 
 */
package com.raritan.tdz.ticket.home;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;

/**
 * this interface will define the common functionality used across ticketing 
 * @author bunty
 *
 */
public interface TicketUtility {

	/**
	 * informs if ticket exists for this item
	 * @param item
	 * @return
	 */
	public boolean ticketExist(Item item);

	/**
	 * informs if the ticket can be updated - completed and archived tickets cannot be updated
	 * @param item
	 * @return
	 */
	public boolean canUpdateTicket(Item item);

	/**
	 * informs if the ticket can be updated - completed and archived tickets cannot be updated
	 * @param tickets
	 * @return
	 */
	public boolean canUpdateTicket(Tickets tickets);

	/**
	 * informs if the ticket can be updated - completed and archived tickets cannot be updated
	 * @param tickets
	 * @return
	 */
	public boolean canUpdateTicket(TicketFields ticketFields);
	
}
