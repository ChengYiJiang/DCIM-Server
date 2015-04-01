package com.raritan.tdz.ticket.home;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class TicketFieldsUpdateHelperImpl implements TicketUpdateHelper {

	@Autowired
	private TicketUtility ticketUtility;
	
	@Autowired
	private TicketFieldsDAO ticketFieldsDAO;

	@Override
	public void update(TicketSaveBehavior behavior, Item item, Object... additionalArgs) {

		/* get the modified ticket field */ 
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldUsingItem(item.getItemId(), true);

		/* check if ticket exist for this item using the ticket util and the state of the ticket allows update */
		if (!ticketUtility.canUpdateTicket(ticketFields)) return;
		
		/* update the location information */
		behavior.updateTicketFields(ticketFields, item);
	
	}
	
	@Override
	public void update(TicketSaveBehavior behavior, Object target, Item item) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the modified ticket fields */ 
		TicketFields ticketFields = (TicketFields) targetMap.get(TicketFields.class.getName());
		
		/* check if the ticket status allow editing */
		if (!ticketUtility.canUpdateTicket(ticketFields.getTickets())) return;

		/* update the location information */
		behavior.updateTicketFields(ticketFields, item);
	
	}
	
}
