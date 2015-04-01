package com.raritan.tdz.ticket.home;

import java.util.List;

public interface TicketSaveBehaviorFactory {

	public List<TicketSaveBehavior> getTicketSaveBehaviors(Long uniqueItemId);
	
}
