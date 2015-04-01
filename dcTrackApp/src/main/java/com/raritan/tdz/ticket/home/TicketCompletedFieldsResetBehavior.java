package com.raritan.tdz.ticket.home;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class TicketCompletedFieldsResetBehavior implements TicketResetBehavior {

	@Autowired
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Override
	public void reset(Object target) {
	
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the ticket */
		Tickets tickets = (Tickets) targetMap.get(Tickets.class.getName());
		if (null == tickets) return;
		
		/* get the modified ticket fields */ 
		TicketFields ticketFields = (TicketFields) targetMap.get(TicketFields.class.getName());
		if (null == ticketFields) return;

		ticketFields.setItem(null);
		ticketFields.setItemId(null);
		
		//Update modified
		ticketFieldsDAO.merge(ticketFields);
		
	}

}
