package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;

public class LocationChangeIPAddressValidator implements Validator {

	@Autowired(required=true)
	private LocationDAO locationDAO;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>) target;
		
		Item item = (Item) targetMap.get(errors.getObjectName());
		
		if (item.getSkipValidation() != null && item.getSkipValidation()) return; 
		
		SavedItemData savedData = SavedItemData.getCurrentItem();
		if (null == savedData) return;
		Item origItem = savedData.getSavedItem();
		if (null == origItem) return;
		
		List<Long> ipAddressIds = itemDAO.getIpAddressId(item.getItemId());
		
		if (null == ipAddressIds || ipAddressIds.size() == 0) return;
		
		if (isSiteChanged(item, origItem)) {
			
			StringBuilder piqIntMessage = new StringBuilder("");
			Long classLkpValueCode = (null != item.getClassLookup()) ? item.getClassLookup().getLkpValueCode() : null;
			if (null != classLkpValueCode && 
					(classLkpValueCode.equals(SystemLookup.Class.RACK_PDU) || classLkpValueCode.equals(SystemLookup.Class.PROBE)) &&
					null != getLocation(item)
					) {
				String piqEnabled = locationDAO.getPiqSettingByLocationId(getLocation(origItem).getDataCenterLocationId(), SystemLookup.ApplicationSettings.PIQ_POLLING_ENABLED);
				if (null != piqEnabled && piqEnabled.equals("true")) {
					piqIntMessage.append("This will delete the ");
					piqIntMessage.append(item.getItemName());
					piqIntMessage.append(" in Power IQ. ");
					piqIntMessage.append("All history and collected data for this ");
					piqIntMessage.append(item.getItemName()); 
					piqIntMessage.append(" will be lost.\n");
				}
			}
			
			Object[] errorArgs = { piqIntMessage.toString() };
			errors.rejectValue("cmbLocation", "ItemValidator.LocationChangeIPAddressDelete", errorArgs, "No slots defined in model library for this chassis model");
			
		}
		
	}

	
	private boolean isSiteChanged (Item item, Item origItem) {
		
		if (item.getItemId() > 0) {
			
			DataCenterLocationDetails origLocation =  getLocation(origItem);
			DataCenterLocationDetails newLocation = getLocation(item);
			if (origLocation != null && newLocation != null) {
				return !(origLocation.getDataCenterLocationId().equals(newLocation.getDataCenterLocationId()));
			}
		}
		return false;
	}
	
	private DataCenterLocationDetails getLocation(Item item) {
		
		return item.getDataCenterLocation();
		
	}

}
