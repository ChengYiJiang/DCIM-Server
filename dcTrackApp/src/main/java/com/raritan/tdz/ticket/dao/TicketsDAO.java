package com.raritan.tdz.ticket.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.Tickets;

public interface TicketsDAO extends Dao<Tickets> {

	public Long getTicketStatusLkpCode(Long ticketId);
	
	public void setTicketStatus(Long ticketId, Long itemId, Long ticketStatus);
	
	public void resetRequestId(Long ticketId);

	/**
	 * get the ticket id for the given item id 
	 * @param itemId
	 * @return
	 */
	public Long getTicketId(Long itemId);
	
}
