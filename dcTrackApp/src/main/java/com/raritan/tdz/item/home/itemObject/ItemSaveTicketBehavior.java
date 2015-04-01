package com.raritan.tdz.item.home.itemObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketPortsDataDAO;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;
import com.raritan.tdz.ticket.home.TicketSaveBehavior;
import com.raritan.tdz.ticket.home.TicketSaveBehaviorFactory;

public class ItemSaveTicketBehavior implements ItemSaveBehavior {

	@Autowired
	private TicketSaveBehaviorFactory ticketSaveBehaviorFactory;
	
	@Autowired
	private TicketsDAO ticketsDAO;
	
	@Autowired
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired
	private TicketPortsDataDAO ticketPortsDataDAO;
	
	@Autowired
	private TicketPortsPowerDAO ticketPortsPowerDAO;
	
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {

		// TODO Auto-generated method stub
		
	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		
		// TODO Auto-generated method stub

	}
	
	private void saveTicketFieldsUsingTicketId(Item item, Object... additionalArgs) {
		Long uniqueItemId = item.getClassMountingFormFactorValue();
		
		List<TicketSaveBehavior> ticketSaveBehaviors = ticketSaveBehaviorFactory.getTicketSaveBehaviors(uniqueItemId);

		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		Errors errors = (Errors) additionalArgs[0];
		
		Long ticketId = item.getTicketId();
		if (null == ticketId || ticketId <= 0) return;
		
		Tickets ticket = ticketsDAO.read(ticketId);
		if (null == ticket) {
			Object[] errorArgs = { };
			errors.rejectValue("Ports", "ExternalTickets.invalidTicket", errorArgs, "invalid ticket id: " + ticketId);
			return;
		}
		
		// add ticket fields
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		if (null == ticketFields) {
			// if the ticket is created with no ticket field data, then the ticket field is not created for this ticket
			// in this case, when the item is getting saved, we have to create the ticket fields and process the modified field
			ticketFieldsDAO.createTicketFieldsUsingTicket(ticket);
			ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		}
		targetMap.put(TicketFields.class.getName(), ticketFields);
		
		// add ticket ports power fields
		List<TicketPortsPower> ticketPortsPower = ticketPortsPowerDAO.getTicketPortsPower(ticketId, true);
		targetMap.put(TicketPortsPower.class.getName(), ticketPortsPower);
		
		// add ticket ports data fields
		List<TicketPortsData> ticketPortsData = ticketPortsDataDAO.getTicketPortsData(ticketId, true);
		targetMap.put(TicketPortsData.class.getName(), ticketPortsData);
		
		for (TicketSaveBehavior ticketSaveBehavior: ticketSaveBehaviors) {
			ticketSaveBehavior.update(targetMap, item, additionalArgs);
		}
		
	}
	
	@SuppressWarnings("unused")
	private void saveTicketFieldsUsingItemId(Item item, Object... additionalArgs) {

		Long uniqueItemId = item.getClassMountingFormFactorValue();
		
		List<TicketSaveBehavior> ticketSaveBehaviors = ticketSaveBehaviorFactory.getTicketSaveBehaviors(uniqueItemId);
		
		for (TicketSaveBehavior ticketSaveBehavior: ticketSaveBehaviors) {
			ticketSaveBehavior.update(item, additionalArgs);
		}

	}
	
	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		// saveTicketFieldsUsingItemId(item, additionalArgs);
		
		saveTicketFieldsUsingTicketId(item, additionalArgs);

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// support all kind of item
		return true;
	}

}
