package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class PlacementCabinetSideTicketSaveBehavior implements
		TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;

	@Override
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		LksData railsUsed = item.getMountedRailLookup();
		
		if (null == railsUsed) {
			ticketFields.setCabinetSide(null);
			
			ticketFields.setCabinetSideId(null);
			
		}
		else {
		
			ticketFields.setCabinetSide(railsUsed.getLkpValue());
			
			ticketFields.setCabinetSideId(railsUsed);
		}
		
		/* merge the updated field */
		ticketFieldsDAO.mergeOnly(ticketFields);
		
	}
	
	@Override
	public void update(Item item, Object... additionalArgs) {

		ticketFieldsUpdateHelperImpl.update(this, item, additionalArgs);

	}

	@Override
	public void update(Object target, Item item, Object... additionalArgs) {
		
		ticketFieldsUpdateHelperImpl.update(this, target, item);
		
	}

}
