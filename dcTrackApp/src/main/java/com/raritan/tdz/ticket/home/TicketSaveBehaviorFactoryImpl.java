package com.raritan.tdz.ticket.home;

import java.util.List;
import java.util.Map;

public class TicketSaveBehaviorFactoryImpl implements TicketSaveBehaviorFactory {

	Map<Long, List<TicketSaveBehavior>> ticketSaveBehaviorMap;
	
	
	
	public Map<Long, List<TicketSaveBehavior>> getTicketSaveBehaviorMap() {
		return ticketSaveBehaviorMap;
	}



	public void setTicketSaveBehaviorMap(
			Map<Long, List<TicketSaveBehavior>> ticketSaveBehaviorMap) {
		this.ticketSaveBehaviorMap = ticketSaveBehaviorMap;
	}
	

	public TicketSaveBehaviorFactoryImpl(
			Map<Long, List<TicketSaveBehavior>> ticketSaveBehaviorMap) {
		super();
		this.ticketSaveBehaviorMap = ticketSaveBehaviorMap;
	}



	@Override
	public List<TicketSaveBehavior> getTicketSaveBehaviors(Long uniqueItemId) {
		
		return ticketSaveBehaviorMap.get(uniqueItemId);
		
	}

}
