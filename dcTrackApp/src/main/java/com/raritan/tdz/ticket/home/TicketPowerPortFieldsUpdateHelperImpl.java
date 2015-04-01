package com.raritan.tdz.ticket.home;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;

public class TicketPowerPortFieldsUpdateHelperImpl implements
		TicketUpdateHelper {

	@Autowired(required=true)
	private TicketUtility ticketUtility;
	
	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketPortsPowerDAO ticketPortsPowerDAO;

	@Override
	public void update(TicketSaveBehavior behavior, Item item, Object... additionalArgs) {

		/* check if ticket exist for this item using the ticket util */
		if (!ticketUtility.canUpdateTicket(item)) return; 
		
		/* get ticket id using item id*/
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldUsingItem(item.getItemId(), true);
		
		/* get the modified ticket field */ 
		List<TicketPortsPower> ticketPortsPowerField = ticketPortsPowerDAO.getTicketPortsPower(ticketFields.getTickets().getTicketId(), true); //ticketFieldsDAO.getTicketFieldUsingItem(item.getItemId(), true);

		/* update the tickets information */
		behavior.updateTicketFields(ticketPortsPowerField, item);
	
	}
	
	@Override
	public void update(TicketSaveBehavior behavior, Object target, Item item) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the modified ticket fields */ 
		@SuppressWarnings("unchecked")
		List<TicketPortsPower> ticketPortsPowerField = (List<TicketPortsPower>) targetMap.get(TicketPortsPower.class.getName());
		
		if (ticketPortsPowerField.size() == 0) return; 
			
		/* check if the ticket status allow editing */
		Tickets tickets = ticketPortsPowerField.get(0).getTicketFields().getTickets();
		if (!ticketUtility.canUpdateTicket(tickets)) return;

		/* update the tickets information */
		behavior.updateTicketFields(ticketPortsPowerField, item);
		
	}
	
}
