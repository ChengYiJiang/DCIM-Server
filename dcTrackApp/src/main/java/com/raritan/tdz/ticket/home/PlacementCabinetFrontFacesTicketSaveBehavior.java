package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class PlacementCabinetFrontFacesTicketSaveBehavior implements
		TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;

	@Override
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		LksData facingLks = null;
		
		Item cabItem = (Item) ticketFieldsDAO.initializeAndUnproxy(item);
		if (cabItem instanceof CabinetItem) {
				
			facingLks = item.getFacingLookup();
		}
		else if (item instanceof ItItem &&
				null != item.getModel() &&
				null != item.getModel().getMounting() &&
				item.getModel().getMounting().equals(SystemLookup.Mounting.FREE_STANDING)) {
			Item container = item.getParentItem();
			if (null != container && container instanceof CabinetItem) {
				
				facingLks = container.getFacingLookup();
			}
		}
		
		if (null == facingLks) {
			ticketFields.setFrontFaces(null);
			
			ticketFields.setFrontFacesId(null);
			
		}
		else {
		
			ticketFields.setFrontFaces(facingLks.getLkpValue());
			
			ticketFields.setFrontFacesId(facingLks);
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
