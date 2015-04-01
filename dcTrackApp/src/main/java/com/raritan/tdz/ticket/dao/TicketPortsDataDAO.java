package com.raritan.tdz.ticket.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.TicketPortsData;

public interface TicketPortsDataDAO extends Dao<TicketPortsData> {
	
	public List<TicketPortsData> getTicketPortsData(Long ticketId, Boolean modified);
	
	public TicketPortsData getOriginalTicketPortData(Long originalTicketFieldId, Long modifiedTicketFieldId, String portName);

}
