package com.raritan.tdz.ticket.home;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketPortsDataDAO;

public class TicketDataPortFieldsUpdateHelperImpl implements TicketUpdateHelper {

	@Autowired(required=true)
	private TicketUtility ticketUtility;
	
	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketPortsDataDAO ticketPortsDataDAO;

	@Override
	public void update(TicketSaveBehavior behavior, Item item, Object... additionalArgs) {

		/* check if ticket exist for this item using the ticket util */
		if (!ticketUtility.canUpdateTicket(item)) return; 
		
		/* get ticket id using item id*/
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldUsingItem(item.getItemId(), true);
		
		/* get the modified ticket field */ 
		List<TicketPortsData> ticketPortsDataField = ticketPortsDataDAO.getTicketPortsData(ticketFields.getTickets().getTicketId(), true);

		/* update the tickets information */
		behavior.updateTicketFields(ticketPortsDataField, item);
	
	}
	
	@Override
	public void update(TicketSaveBehavior behavior, Object target, Item item) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the modified ticket fields */ 
		@SuppressWarnings("unchecked")
		List<TicketPortsData> ticketPortsDataField = (List<TicketPortsData>) targetMap.get(TicketPortsData.class.getName());
		
		if (ticketPortsDataField.size() == 0) return;

		/* check if the ticket status allow editing */
		Tickets tickets = ticketPortsDataField.get(0).getTicketFields().getTickets();
		if (!ticketUtility.canUpdateTicket(tickets)) return;

		/* update the tickets information */
		behavior.updateTicketFields(ticketPortsDataField, item);
	
	}
	
}
