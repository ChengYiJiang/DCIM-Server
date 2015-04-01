package com.raritan.tdz.ticket.home;

import java.util.Calendar;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;

public class TicketsResetBehavior implements TicketResetBehavior {

	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired
	private TicketsDAO ticketsDAO; 
	
	@Override
	public void reset(Object target) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the ticket */
		Tickets tickets = (Tickets) targetMap.get(Tickets.class.getName());
		
		if (null == tickets) return;

		tickets.setStatusLks(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.TicketStatus.RECEIVED).get(0));
		
		tickets.setUpdateDate(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
		
		tickets.setRequest(null);
		
		tickets.setMissingFields(null);
		
		ticketsDAO.merge(tickets);
		
	}

}
