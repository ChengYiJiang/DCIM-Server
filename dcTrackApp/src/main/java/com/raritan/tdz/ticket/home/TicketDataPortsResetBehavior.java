package com.raritan.tdz.ticket.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.ticket.dao.TicketPortsDataDAO;
import com.raritan.tdz.util.TicketCommonUtil;

public class TicketDataPortsResetBehavior implements TicketResetBehavior {

	// private final Logger log = Logger.getLogger( this.getClass() );
	
	@Autowired
	private TicketPortsDataDAO ticketPortsDataDAO;
	
	@Override
	public void reset(Object target) {
	
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		/* get the ticket */
		Tickets tickets = (Tickets) targetMap.get(Tickets.class.getName());
		if (null == tickets) return;
		Long ticketId = tickets.getTicketId();

		// Get the modified ticket fields
		TicketFields ticketFields = (TicketFields) targetMap.get(TicketFields.class.getName());
		if (null == ticketFields) return;

		// Get the modified data ports and collect the ids
		@SuppressWarnings("unchecked")
		List<TicketPortsData> ticketPortsDataMod = (List<TicketPortsData>) targetMap.get(TicketPortsData.class.getName());
		if (null == ticketPortsDataMod) return;

		// Collect the the id for the modified data ports to merge
		List<Long> ids = new ArrayList<Long>();
		for (TicketPortsData ticketPortData: ticketPortsDataMod) {
			ids.add(ticketPortData.getTicketPortId());
		}	
		
		// get the original ticket's data ports
		List<TicketPortsData> ticketPortsData = ticketPortsDataDAO.getTicketPortsData(ticketId, false);
		if (null == ticketPortsData) return;

		// Go through all the original data port and update the corresponding modified data port ticket fields
		// Iterator<Long> iterator = ids.iterator();
		TicketCommonUtil ticketCommonUtil = new TicketCommonUtil(ticketPortsDataDAO.getSession());
		for (TicketPortsData ticketPortData: ticketPortsData) {
			
			// create modified data port
			TicketPortsData refModticketPortsData = getModifiedPortUsingPairIndex(ticketPortsDataMod, ticketPortData.getPairIndex()); //(TicketPortsData) ticketPortData.clone();
			
			TicketPortsData mod_ticketPortsData = (TicketPortsData) ticketPortData.clone();

			// set the pair index
			mod_ticketPortsData.setPairIndex(ticketPortData.getPairIndex());

			// update the modified ticket field value
			mod_ticketPortsData.setTicketFields(ticketFields);
			
			// update the is modified data
			mod_ticketPortsData.setIsModified(ticketFields.getIsModified());
			
			//Fill in the lookups within the ticket ports data
			ticketCommonUtil.fillLookupsInTicketPortsData(mod_ticketPortsData);

			if (null != refModticketPortsData) {
				// set the ticket port id
				mod_ticketPortsData.setTicketPortId(refModticketPortsData.getTicketPortId());
				// update modified
				ticketPortsDataDAO.merge(mod_ticketPortsData);
			}
			else {
				// Save modified
				// this takes care of the case when there are more original data ports and less corresponding modified fields
				ticketPortsDataDAO.create(mod_ticketPortsData);
			}
			
		}
		
		// Handle the case when there are more modified data ports than the original set of data ports
		/*for (;iterator.hasNext();) {
			log.debug(iterator.next() + " : Extra modified data port, got to delete it...");
		}*/
		
	}
	
	private TicketPortsData getModifiedPortUsingPairIndex(List<TicketPortsData> ticketPortsDataMod, Integer pairIndex) {
		
		if (null == ticketPortsDataMod || null == pairIndex) return null;
		
		for (TicketPortsData modDataPort: ticketPortsDataMod) {
			if (modDataPort.getPairIndex().equals(pairIndex)) {
				return modDataPort;
			}
		}
		
		return null;
	}
	

}
