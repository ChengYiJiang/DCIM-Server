package com.raritan.tdz.ticket.home;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.util.TicketCommonUtil;

public class TicketFieldsResetBehavior implements TicketResetBehavior {

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
		
		// Get the original ticket field
		TicketFields ticketFieldsOriginal = ticketFieldsDAO.getTicketFieldsUsingTicket(tickets.getTicketId(), false);
		if (null == ticketFieldsOriginal) return;
		
		//Create the modified record and mark the isModified to true
		TicketFields mod_ticketFields = (TicketFields) ticketFieldsOriginal.clone();
		mod_ticketFields.setIsModified(true);
		mod_ticketFields.setTicketFieldId(ticketFields.getTicketFieldId());
		
		//Fill in the lookups within the ticket fields
		TicketCommonUtil ticketCommonUtil = new TicketCommonUtil(ticketFieldsDAO.getSession());
		
		try {
			
			ticketCommonUtil.fillLookupsInTicketFields(mod_ticketFields);
			//Save modified
			ticketFieldsDAO.merge(mod_ticketFields);
			
		} catch (DataAccessException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
