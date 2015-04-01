package com.raritan.tdz.ticket.home;

import java.util.List;

public class TicketMultipleResetBehavior implements TicketResetBehavior {

	List<TicketResetBehavior> ticketDeleteBehaviors;
	
	public List<TicketResetBehavior> getTicketDeleteBehaviors() {
		return ticketDeleteBehaviors;
	}



	public void setTicketDeleteBehaviors(
			List<TicketResetBehavior> ticketDeleteBehaviors) {
		this.ticketDeleteBehaviors = ticketDeleteBehaviors;
	}



	public TicketMultipleResetBehavior(List<TicketResetBehavior> ticketDeleteBehaviors) {
		
		this.ticketDeleteBehaviors = ticketDeleteBehaviors;
	}


	
	@Override
	public void reset(Object target) {
		
		for (TicketResetBehavior behavior: ticketDeleteBehaviors) {
			behavior.reset(target);
		}


	}

}
