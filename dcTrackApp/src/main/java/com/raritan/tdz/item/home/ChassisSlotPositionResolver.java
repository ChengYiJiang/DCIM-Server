package com.raritan.tdz.item.home;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.UiIdLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;

public class ChassisSlotPositionResolver implements ItemDataResolver {
	
	@Autowired
	SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired
	ChassisHome chassisHome;
	
	@Autowired
	ItemDAO itemDAO;
	
	@Override
	public void resolve(Map<String, Object> data, Errors errors) throws DataAccessException, BusinessValidationException {
		
		Object slotLabelObj = data.get(UiIdLookup.UiId.CMB_SLOT_POSITION);
		Long faceLkpValueCode = (Long)data.get(UiIdLookup.UiId.RADIO_CHASSISFACE);
		String location = (String)data.get(UiIdLookup.UiId.CMB_LOCATION);
		String chassisName = (String)data.get(UiIdLookup.UiId.CMB_CHASSIS);
		String itemName = (String)data.get(UiIdLookup.UiId.TI_NAME);

		Long chassisId = null;
		String slotlabel = null;
		
		if (slotLabelObj == null) return; 
		
		if (slotLabelObj instanceof Integer) 
			slotlabel = slotLabelObj.toString();
		else 
			slotlabel = (String)slotLabelObj; 
		
		if (slotlabel.isEmpty()) {
			data.put(UiIdLookup.UiId.CMB_SLOT_POSITION, -9);
			return;
		}
		
		if (location != null && chassisName != null ) {
			chassisId = itemDAO.getItemByLocationAndName(location, chassisName);
		} else {
			Item item = null;
			// check if this is an existing blade item
			Long itemId = itemDAO.getItemByLocationAndName(location, itemName);
			if (itemId != null && itemId > 0) item = itemDAO.getItem(itemId);
			if (item != null && item.getSubclassLookup() != null && 
					(item.getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.BLADE ||
					item.getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.BLADE_SERVER)) {
				Item chassisItem = ((ItItem)item).getBladeChassis();
				if (chassisItem != null) {
					chassisId = chassisItem.getItemId();
					if (faceLkpValueCode == null) {
						if (chassisItem.getFacingLookup() != null) {
							faceLkpValueCode = chassisItem.getFacingLookup().getLkpValueCode();
						}
					}
				}
			}
		}

		List<ChassisSlotDTO>  slotDtoList = null;
		if (chassisId != null && chassisId > 0 && faceLkpValueCode !=null) {
			slotDtoList = chassisHome.getChassisSlotDetails(chassisId, faceLkpValueCode);
		}

		Integer slotNumber = null;
		if (slotDtoList != null) {
			for (ChassisSlotDTO csdto : slotDtoList) {
				if (csdto.getLabel().equals(slotlabel)) {
					slotNumber = csdto.getNumber();
					break;
				}
			}
		}

		if (slotNumber != null ) data.put(UiIdLookup.UiId.CMB_SLOT_POSITION, slotNumber);
	}

}
