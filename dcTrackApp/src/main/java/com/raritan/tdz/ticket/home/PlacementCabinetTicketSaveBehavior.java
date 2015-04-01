package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class PlacementCabinetTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;

	@Override
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		if (null == item.getParentItem()) {
			ticketFields.setCabinetName(null);
			ticketFields.setCabinet(null);
			
		}
		else {
		
			CabinetItem cabItem = (CabinetItem) ticketFieldsDAO.initializeAndUnproxy(item.getParentItem());
			/* update the cabinet_name in the ticket fields */
			ticketFields.setCabinetName(cabItem.getItemName());
			
			ticketFields.setCabinet(cabItem);
			
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
