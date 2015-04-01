package com.raritan.tdz.ticket.home;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;
import com.raritan.tdz.util.TicketCommonUtil;

public class TicketPowerSupplyResetBehavior implements TicketResetBehavior {

	@Autowired
	private TicketPortsPowerDAO ticketPortsPowerDAO;
	
	@Override
	public void reset(Object target) {
	
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the ticket */
		Tickets tickets = (Tickets) targetMap.get(Tickets.class.getName());
		if (null == tickets) return;
		Long ticketId = tickets.getTicketId();
		
		/* get the ticket fields */
		TicketFields ticketFields = (TicketFields) targetMap.get(TicketFields.class.getName());
		if (null == ticketFields) return;
		
		/* get the modified power supply port */
		TicketPortsPower ticketPortPowerSupplyMod = (TicketPortsPower) targetMap.get(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.POWER_SUPPLY);
		
		if (null == ticketPortPowerSupplyMod) return;
		
		// get the original ticket's power supply ports
		TicketPortsPower ticketPortPower = ticketPortsPowerDAO.getTicketPortPowerSupply(ticketId, false);
		
		if (null == ticketPortPower) return;
		
		TicketCommonUtil ticketCommonUtil = new TicketCommonUtil(ticketPortsPowerDAO.getSession());
			
		// create modified data port
		TicketPortsPower mod_ticketPortsPower = (TicketPortsPower) ticketPortPower.clone();
		
		// update the modified ticket field value
		mod_ticketPortsPower.setTicketFields(ticketFields);
		
		// update the is modified data
		mod_ticketPortsPower.setIsModified(ticketFields.getIsModified());
		
		mod_ticketPortsPower.setTicketPortId(ticketPortPowerSupplyMod.getTicketPortId());
		
		//Fill in the lookups within the ticket ports data
		ticketCommonUtil.fillLookupsInTicketPortsPowerSupply(mod_ticketPortsPower);
		
		//Save modified
		ticketPortsPowerDAO.merge(mod_ticketPortsPower);
		
	}


}
