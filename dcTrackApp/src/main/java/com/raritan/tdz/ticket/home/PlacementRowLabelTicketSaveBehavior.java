package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class PlacementRowLabelTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;

	@Override
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		Item cabItem = (Item) ticketFieldsDAO.initializeAndUnproxy(item);
		if (cabItem instanceof CabinetItem) {
				/* update the row label in the ticket fields */
				ticketFields.setRowLabel(((CabinetItem)cabItem).getRowLabel());
		}
		else if (cabItem instanceof ItItem &&
				null != cabItem.getModel() &&
				null != cabItem.getModel().getMounting() &&
				cabItem.getModel().getMounting().equals(SystemLookup.Mounting.FREE_STANDING)) {
			Item container = cabItem.getParentItem();
			if (null != container && container instanceof CabinetItem) {
				ticketFields.setRowLabel(((CabinetItem)container).getRowLabel());
			}
		}
		else {
			ticketFields.setRowLabel(null);
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
