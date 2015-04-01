package com.raritan.tdz.ticket.home;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.ticket.dao.TicketsDAO;

public class TicketsCompletedResetBehavior implements TicketResetBehavior {

	@Autowired
	private TicketsDAO ticketsDAO; 
	
	@Override
	public void reset(Object target) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the ticket */
		Tickets tickets = (Tickets) targetMap.get(Tickets.class.getName());
		
		if (null == tickets) return;

		tickets.setRequest(null);
		
		tickets.setMissingFields(null);
		
		ticketsDAO.merge(tickets);
		
	}

}
