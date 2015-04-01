package com.raritan.tdz.item.home;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.UiIdLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;


public class ShelfPositionResolver implements ItemDataResolver {
	
	@Autowired
	ItemDAO itemDAO;
	
	@Autowired
	ItemHome itemHome;
	
	@Autowired
	SystemLookupFinderDAO systemLookupFinderDAO;

	@Override
	public void resolve(Map<String, Object> data, Errors errors) {
		
		Integer shelfPosition = (Integer)data.get(UiIdLookup.UiId.CMB_ORDER);
		String cabinetName = (String)data.get(UiIdLookup.UiId.CMB_CABINET);
		Object uPositionObj = data.get(UiIdLookup.UiId.CMB_U_POSITION);
		String railsUsed = (String)data.get(UiIdLookup.UiId.RADIO_RAILS_USED);
		String itemName = (String)data.get(UiIdLookup.UiId.TI_NAME);
		String location = (String)data.get(UiIdLookup.UiId.CMB_LOCATION);
		Long editItemId = null;
		Long cabinetId = null;
		Long uPosition = null;
		
		if (uPositionObj instanceof String) {
			// no need to proceed further because 
			// uPosition may not be valid
			// ItemUPositionValidator will catch the error
			return;
		}
		else if (uPositionObj instanceof Long) {
			uPosition = (Long)uPositionObj;
		}
		
		LksData railsUsedLks = null;
		 List<LksData> ru = systemLookupFinderDAO.findByLkpTypeNameAndLkpValue("RAILS_USED", railsUsed != null? railsUsed :"Front");
		 if (ru != null && ru.size() > 0) railsUsedLks = ru.get(0);
		 
		// get the itemID of the item being edited
		if (itemName != null && location != null) {
			editItemId = itemDAO.getItemByLocationAndName(location, itemName);
			// getAvailableShelfPosition call below does not take 
			// null itemId therefore setting it to 0
			if (editItemId == null)editItemId = 0L; 
		}
		
		if (cabinetName != null && location != null) {
			cabinetId = itemDAO.getItemByLocationAndName(location, cabinetName);
		}
		
		// get next available position for new item only
		if ((editItemId !=null &&  editItemId == 0L) && (shelfPosition == null || shelfPosition == 0)) {
			if (cabinetId != null && railsUsedLks != null && uPosition != null) {
				Long railsUsedLkpValueCode = railsUsedLks.getLkpValueCode();
				Collection<Integer> shelfPositions = 
						itemHome.getAvailableShelfPosition(cabinetId, uPosition, railsUsedLkpValueCode, editItemId);
				if (shelfPositions.size() > 0) {
					Integer position = shelfPositions.iterator().next();
					data.put(UiIdLookup.UiId.CMB_ORDER, position);
				}
					
			}
		}
	}
}
