package com.raritan.tdz.ticket.home;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;
import com.raritan.tdz.util.TicketCommonUtil;

public class TicketRpduOutletPortsResetBehavior implements TicketResetBehavior {

	private final Logger log = Logger.getLogger( this.getClass() );

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
		
		@SuppressWarnings("unchecked")
		List<TicketPortsPower> ticketPortsPowerRpduOulets = (List<TicketPortsPower>) targetMap.get(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.RACK_PDU_OUTPUT);
		if (null == ticketPortsPowerRpduOulets) return;
		
		List<Long> ids = new ArrayList<Long>();
		for (TicketPortsPower ticketPortPower: ticketPortsPowerRpduOulets) {
			ids.add(ticketPortPower.getTicketPortId());
		}	

		// Get the modified ticket fields
		TicketFields ticketFields = (TicketFields) targetMap.get(TicketFields.class.getName());
		if (null == ticketFields) return;
		
		// get the modified rpdu inlet ports
		@SuppressWarnings("unchecked")
		List<TicketPortsPower> rpduInletPortFields = (List<TicketPortsPower>) targetMap.get(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.INPUT_CORD);
		if (null == rpduInletPortFields) return;
		
		// get the original ticket's rpdu outlet ports
		List<TicketPortsPower> ticketPortsPower = ticketPortsPowerDAO.getTicketPortsPowerRpduOutlets(ticketId, false);
		if (null == ticketPortsPower) return;
		
		Iterator<Long> iterator = ids.iterator();
		TicketCommonUtil ticketCommonUtil = new TicketCommonUtil(ticketPortsPowerDAO.getSession());
		for (TicketPortsPower ticketPortPower: ticketPortsPower) {
			
			// create modified data port
			TicketPortsPower mod_ticketPortsPower = (TicketPortsPower) ticketPortPower.clone();
			
			// update the modified ticket field value
			mod_ticketPortsPower.setTicketFields(ticketFields);
			
			// update the is modified data
			mod_ticketPortsPower.setIsModified(ticketFields.getIsModified());
			
			//Fill in the lookups within the ticket ports data
			ticketCommonUtil.fillLookupsInTicketPortsRpduOutlet(mod_ticketPortsPower, rpduInletPortFields);
			
			if (iterator.hasNext()) {
				mod_ticketPortsPower.setTicketPortId(iterator.next());
				//Update modified
				ticketPortsPowerDAO.merge(mod_ticketPortsPower);
			}
			else {
				//Save modified
				// this takes care of the case when there are more original rack pdu outlets and less corresponding modified fields
				ticketPortsPowerDAO.create(mod_ticketPortsPower);

			}

		}
		
		// Handle the case when there are more modified rack pdu outlets than the original set of rack pdu outlets 
		for (;iterator.hasNext();) {
			log.debug(iterator.next() + " : Extra modified rack pdu outlet, got to delete it...");
		}

		
	}

}
