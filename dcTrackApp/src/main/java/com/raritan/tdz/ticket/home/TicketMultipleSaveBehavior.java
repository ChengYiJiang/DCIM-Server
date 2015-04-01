package com.raritan.tdz.ticket.home;

import java.util.List;


import com.raritan.tdz.domain.Item;

public class TicketMultipleSaveBehavior implements TicketSaveBehavior {

	List<TicketSaveBehavior> ticketSaveBehaviors;
	
	public TicketMultipleSaveBehavior(List<TicketSaveBehavior> ticketSaveBehaviors) {
		
		this.ticketSaveBehaviors = ticketSaveBehaviors;
	}

	public List<TicketSaveBehavior> getTicketSaveBehaviors() {
		return ticketSaveBehaviors;
	}

	public void setTicketSaveBehaviors(List<TicketSaveBehavior> ticketSaveBehaviors) {
		this.ticketSaveBehaviors = ticketSaveBehaviors;
	}

	@Override
	public void update(Item item, Object... additionalArgs) {

		for (TicketSaveBehavior behavior: ticketSaveBehaviors) {
			behavior.update(item, additionalArgs);
		}

	}

	@Override
	public void update(Object target, Item item, Object... additionalArgs) {
		
		for (TicketSaveBehavior behavior: ticketSaveBehaviors) {
			behavior.update(target, item, additionalArgs);
		}

	}

	@Override
	public void updateTicketFields(Object fields, Item item) {
		// TODO Auto-generated method stub
		
	}

}
