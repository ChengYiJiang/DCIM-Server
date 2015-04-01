package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class PlacementChassisTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;

	@Override
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		ItItem itItem = (ItItem) ticketFieldsDAO.initializeAndUnproxy(item);

		if (null == itItem.getBladeChassis()) {

			/* update the chassis name in the ticket fields */
			ticketFields.setChassis(null);
			ticketFields.setChassisItem(null);
			
		}
		else {
			ItItem chassisItem = (ItItem) ticketFieldsDAO.initializeAndUnproxy(itItem.getBladeChassis());
			
			/* update the chassis name in the ticket fields */
			ticketFields.setChassis(chassisItem.getItemName());
			 
			/* update the chassis_id in the ticket fields */
			ticketFields.setChassisItem(chassisItem);
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
