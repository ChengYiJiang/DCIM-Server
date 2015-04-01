package com.raritan.tdz.ticket.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;

public interface TicketFieldsDAO extends Dao<TicketFields> {

	/**
	 * get the ticket field associated with the item
	 * @param itemId
	 * @return
	 */
	public List<TicketFields> getTicketFieldUsingItem(Long itemId);
	
	/**
	 * get the list of ticket fields associated with the ticket
	 * @param ticketId
	 * @return
	 */
	public List<TicketFields> getTicketFieldsUsingTicket(Long ticketId);
	
	/**
	 * get the ticket field filtered with modified field using the ticket id
	 * @param ticketId
	 * @param modified
	 * @return
	 */
	public TicketFields getTicketFieldsUsingTicket(Long ticketId, Boolean modified);
	
	/**
	 * get the ticket field filtered with modified field using the item id
	 * @param itemId
	 * @param modified
	 * @return
	 */
	public TicketFields getTicketFieldUsingItem(Long itemId, Boolean modified);

	/**
	 * create the ticket fields if the original ticket do not have the field values
	 * @param tickets
	 */
	public void createTicketFieldsUsingTicket(Tickets tickets);
	
}
