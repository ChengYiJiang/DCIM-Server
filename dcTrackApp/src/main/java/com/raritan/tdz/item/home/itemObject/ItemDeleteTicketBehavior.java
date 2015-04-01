package com.raritan.tdz.item.home.itemObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketPortsDataDAO;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;
import com.raritan.tdz.ticket.home.TicketResetBehavior;
import com.raritan.tdz.ticket.home.TicketUtility;

public class ItemDeleteTicketBehavior implements ItemDeleteBehavior {

	@Autowired
	private TicketsDAO ticketsDAO;
	
	@Autowired
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired
	private TicketPortsDataDAO ticketPortsDataDAO;
	
	@Autowired
	private TicketPortsPowerDAO ticketPortsPowerDAO;
	
	@Autowired
	private TicketResetBehavior ticketResetBehaviors;
	
	@Autowired
	private TicketResetBehavior ticketCompletedResetBehaviors;
	
	@Autowired
	private TicketUtility ticketUtility;

	
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	public void preDelete(Item item) throws BusinessValidationException {

		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		Long ticketId = ticketsDAO.getTicketId(item.getItemId());
		if (null == ticketId || ticketId <= 0) return;
		
		Tickets ticket = ticketsDAO.read(ticketId);
		
		targetMap.put(Tickets.class.getName(), ticket);
		
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		
		targetMap.put(TicketFields.class.getName(), ticketFields);
		// delete the modified ticket field
		// ticketFieldsDAO.delete(ticketFields);

		// add power supply port
		TicketPortsPower powerSupplyPort = ticketPortsPowerDAO.getTicketPortPowerSupply(ticketId, true);
		targetMap.put(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.POWER_SUPPLY, powerSupplyPort);
		
		// add ticket rack pdu inlet ports fields
		List<TicketPortsPower> ticketPortsPowerRpduInlet = ticketPortsPowerDAO.getTicketPortsPowerRpduInlets(ticketId, true);
		targetMap.put(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.INPUT_CORD, ticketPortsPowerRpduInlet);

		// add ticket rack pdu outlet ports fields
		List<TicketPortsPower> ticketPortsPowerRpduOutlet = ticketPortsPowerDAO.getTicketPortsPowerRpduOutlets(ticketId, true);
		targetMap.put(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.RACK_PDU_OUTPUT, ticketPortsPowerRpduOutlet);

		// add ticket ports data fields
		List<TicketPortsData> ticketPortsData = ticketPortsDataDAO.getTicketPortsData(ticketId, true);
		targetMap.put(TicketPortsData.class.getName(), ticketPortsData);

		// check if the ticket can be updated
		if (ticketUtility.canUpdateTicket(ticket)) {
		
			ticketResetBehaviors.reset(targetMap);
		}
		else {
		
			ticketCompletedResetBehaviors.reset(targetMap);
		}
		
	}

	@Override
	public void postDelete() throws BusinessValidationException {
		// TODO Auto-generated method stub

	}


}
