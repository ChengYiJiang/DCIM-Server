package com.raritan.tdz.ticket.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.TicketPortsPower;

public interface TicketPortsPowerDAO extends Dao<TicketPortsPower> {

	public List<TicketPortsPower> getTicketPortsPower(Long ticketId, Boolean modified);

	public TicketPortsPower getTicketPortPowerSupply(Long ticketId, Boolean modified);

	public List<TicketPortsPower> getTicketPortsPowerRpduInlets(Long ticketId, Boolean modified);

	public List<TicketPortsPower> getTicketPortsPowerRpduOutlets(Long ticketId, Boolean modified);
	
}
