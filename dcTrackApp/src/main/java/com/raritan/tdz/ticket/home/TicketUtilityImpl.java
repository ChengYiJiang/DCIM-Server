package com.raritan.tdz.ticket.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;

public class TicketUtilityImpl implements TicketUtility {

	/*@Autowired
	private ExternalTicketFinderDAO externalTicketFinderDAO;*/
	
	@Autowired
	TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired
	private TicketsDAO ticketsDAO;
	
	List<Long> ticketStateNotEditable;
	
	public List<Long> getTicketStateNotEditable() {
		return ticketStateNotEditable;
	}

	public void setTicketStateNotEditable(List<Long> ticketStateNotEditable) {
		this.ticketStateNotEditable = ticketStateNotEditable;
	}

	@Override
	public boolean ticketExist(Item item) {
		
		Long ticketId = item.getTicketId();

		return (null != ticketId && ticketId > 0);
	}

	@Override
	public boolean canUpdateTicket(Item item) {

		if (!ticketExist(item)) return false;
		
		Long status = ticketsDAO.getTicketStatusLkpCode(item.getTicketId());
		
		return !ticketStateNotEditable.contains(status);
		
	}
	
	@Override
	public boolean canUpdateTicket(Tickets tickets) {

		if (null == tickets || null == tickets.getTicketStatus() || null == tickets.getTicketStatus().getStatusLookup()) {
			return false;
		}
		
		Long status = tickets.getTicketStatus().getStatusLookup().getLkpValueCode();
		
		return !ticketStateNotEditable.contains(status);
		
	}

	@Override
	public boolean canUpdateTicket(TicketFields ticketFields) {

		if (null == ticketFields || null == ticketFields.getTickets()) return false;
		
		Tickets tickets = ticketFields.getTickets();

		return canUpdateTicket(tickets);
		
	}


}
