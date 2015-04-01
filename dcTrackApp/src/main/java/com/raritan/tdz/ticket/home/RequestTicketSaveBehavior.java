package com.raritan.tdz.ticket.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.item.request.ItemRequest.ItemRequestType;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;

public class RequestTicketSaveBehavior implements TicketSaveBehavior {

	
	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired(required=true)
	private TicketsDAO ticketsDAO;
	
	@Override
	public void update(Item item, Object... additionalArgs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Object target, Item item, Object... additionalArgs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTicketFields(Object map, Item item) {

		@SuppressWarnings("unchecked")
		Map<Long, Long> itemRequestMap = (Map<Long, Long>) map;
		
		updateTicketsRequest(itemRequestMap);

	}
	
	/**
	 * get the map of item_ids and the request ids
	 * @param itemRequestMap
	 */
	private void updateTicketsRequest(Map<Long, Long> itemRequestMap) {

		if (null == itemRequestMap) return;
		
		for (Map.Entry<Long, Long> entry: itemRequestMap.entrySet()) {
			Long itemId  = entry.getKey();
			Long requestId = entry.getValue();

			if (null == itemId || null == requestId) return;
			
			// Check if the current ticket status allows status update
			TicketFields ticketFields = ticketFieldsDAO.getTicketFieldUsingItem(itemId, true);
			if (null != ticketFields) {
				List<Long> allowedStatusUpdateInStates = new ArrayList<Long>();
				allowedStatusUpdateInStates.add(SystemLookup.TicketStatus.INCOMPLETE);
				allowedStatusUpdateInStates.add(SystemLookup.TicketStatus.UPDATED);
				allowedStatusUpdateInStates.add(SystemLookup.TicketStatus.RECEIVED);
				
				Tickets tickets = ticketFields.getTickets();
				LksData currentStatus = tickets.getStatusLks();
				if (null != currentStatus) {
					Long currStatusLkpCode = currentStatus.getLkpValueCode();
					if (!allowedStatusUpdateInStates.contains(currStatusLkpCode)) {
						return;
					}
				}
				
				// Check the request type, if not an install request do not update ticket
				Request request = itemRequestDAO.loadRequest(requestId);
				if (null != request && !request.getRequestType().equals(ItemRequestType.installItem)) {
					return;
				}
				
				// update the request id to the ticket
				tickets.setStatusLks(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.TicketStatus.REQUEST_ISSUED).get(0));
				tickets.setRequest(request);
				ticketsDAO.mergeOnly(tickets);
			}

			
		}
		
	}


}
