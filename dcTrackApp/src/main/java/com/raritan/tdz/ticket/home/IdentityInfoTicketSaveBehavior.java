package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class IdentityInfoTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;


	private void updateItemPurpose(TicketFields ticketFields, Item item) {

		ItemServiceDetails itemServiceDetails = item.getItemServiceDetails();
		
		if (null == itemServiceDetails) {
			ticketFields.setPurpose(null);
			ticketFields.setPurposeLookup(null);
			return;
		}
		
		LkuData purposeLku = itemServiceDetails.getPurposeLookup();
		
		if (null == purposeLku) {

			ticketFields.setPurpose(null);
		}
		else {
			/* update the item_class in the ticket fields */
			ticketFields.setPurpose(purposeLku.getLkuValue());
		}
			
		/* update the class_lks_id in the ticket fields */
		ticketFields.setPurposeLookup(purposeLku);
		
	}

	private void updateItemFunction(TicketFields ticketFields, Item item) {

		ItemServiceDetails itemServiceDetails = item.getItemServiceDetails();
		
		if (null == itemServiceDetails) {
			ticketFields.setFunction(null);
			ticketFields.setFunctionLookup(null);
			return;
		}
		
		LkuData functionLku = itemServiceDetails.getFunctionLookup();
		
		if (null == functionLku) {

			ticketFields.setFunction(null);
		}
		else {
			/* update the item_class in the ticket fields */
			ticketFields.setFunction(functionLku.getLkuValue());
		}
			
		/* update the class_lks_id in the ticket fields */
		ticketFields.setFunctionLookup(functionLku);
		
	}

	private void updateItemName(TicketFields ticketFields, Item item) {

		ticketFields.setItemName(item.getItemName());
		
	}


	@Override
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		/* update item purpose */
		updateItemPurpose(ticketFields, item);
		
		/* update item function */
		updateItemFunction(ticketFields, item);

		/* update the make information */
		updateItemName(ticketFields, item);
		
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
