package com.raritan.tdz.ticket.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.ModelMfrDetails;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;

public class HardwareInfoTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketFieldsUpdateHelperImpl;


	private void updateItemClass(TicketFields ticketFields, Item item) {

		/* get the item class */
		LksData itemClass = item.getClassLookup();
		
		if (null == itemClass) {
			ticketFields.setItemClass(null);
			ticketFields.setClassLookup(null);
			return;
		}

		/* update the item_class in the ticket fields */
		ticketFields.setItemClass(itemClass.getLkpValue());
			
		/* update the class_lks_id in the ticket fields */
		ticketFields.setClassLookup(itemClass);
		
	}

	private void updateItemSubClass(TicketFields ticketFields, Item item) {

		/* get the item class */
		LksData itemSubClass = item.getSubclassLookup();
		
		if (null == itemSubClass) {
			ticketFields.setSubClass(null);
			ticketFields.setSubclassLookup(null);
			return;
		}
			
		/* update the item_subclass in the ticket fields */
		ticketFields.setSubClass(itemSubClass.getLkpValue());
		
		/* update the subclass_lks_id in the ticket fields */
		ticketFields.setSubclassLookup(itemSubClass);
		
	}

	private void updateItemMake(TicketFields ticketFields, Item item) {

		if (null == item.getModel()) {
			ticketFields.setMake(null);
			ticketFields.setModelMfrDetails(null);
			return;
		}
		
		/* get the item class */
		ModelMfrDetails mfrDetails = item.getModel().getModelMfrDetails();
		
		if (null == mfrDetails) {
			ticketFields.setMake(null);
			ticketFields.setModelMfrDetails(null);
			return;
		}
			
		/* update the make in the ticket fields */
		ticketFields.setMake(mfrDetails.getMfrName());
		
		/* update the mfr_id in the ticket fields */
		ticketFields.setModelMfrDetails(mfrDetails);
		
	}

	private void updateItemModel(TicketFields ticketFields, Item item) {

		ModelDetails model = item.getModel();
		
		if (null == model) {
			ticketFields.setModel(null);
			ticketFields.setModelDetails(null);
			return;
		}
		
		/* update the model in the ticket fields */
		ticketFields.setModel(model.getModelName());
		
		/* update the model_id in the ticket fields */
		ticketFields.setModelDetails(model);
		
	}
	
	private void updateItemAssetTag(TicketFields ticketFields, Item item) {
		ItemServiceDetails itemServiceDetails = item.getItemServiceDetails();
		
		if (null == itemServiceDetails) {
			ticketFields.setAssetNumber(null);
			ticketFields.setSerialNumber(null);
			return;
		}
		
		/* update the asset tag */
		ticketFields.setAssetNumber(itemServiceDetails.getAssetNumber());
		
		/* update the serial number */
		ticketFields.setSerialNumber(itemServiceDetails.getSerialNumber());
	}

	@Override
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketFields ticketFields = (TicketFields) fields;
		
		/* update item class */
		updateItemClass(ticketFields, item);
		
		/* update item subclass */
		updateItemSubClass(ticketFields, item);

		/* update the make information */
		updateItemMake(ticketFields, item);
		
		/* update the model */
		updateItemModel(ticketFields, item);
		
		/* update the asset and serial number */
		updateItemAssetTag(ticketFields, item);
		
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
