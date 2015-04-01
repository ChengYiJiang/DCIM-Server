package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

/**
 * This class will update all the placement information that is common across all the mounting type
 * @author bunty
 *
 */
public class PlacementLocationTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;
	
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		DataCenterLocationDetails locationDetails = item.getDataCenterLocation();
		
		if (null == locationDetails) {
			
			ticketFields.setDataCenterCode(null);
		}
		else {

			/* update the dc_code in the ticket fields */
			ticketFields.setDataCenterCode(locationDetails.getCode());
		}
		
		/* update the location_id in the ticket fields */
		ticketFields.setDataCenterLocationDetails(locationDetails);
		
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
