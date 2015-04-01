/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class NRPlacementValidator implements Validator {
	
	@Autowired
	private ItemHome itemHome;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return ItItem.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Object itemDomainObject = targetMap.get(errors.getObjectName());
		
		/* Validate the Non-Rackable item
		1. Validate if the shelf position is valid
		2. Validate U position 
		3. Validate Rails Used */
		validateRailsUsed(itemDomainObject, errors);
		validateShelfPosition(itemDomainObject, errors);

	}
	
	private void validateShelfPosition(Object target, Errors errors) {
		Item item = (Item)target;
		Collection<Integer> availablePositions = null;
		// Do not validate Shelf position if the parent (Cabinet) is not provided OR if the U position is not selected
		if (null == item.getParentItem() || item.getuPosition() == -9) {
			return;
		}
		try {
			if (null != item.getParentItem() && null != item.getModel() && null != item.getMountedRailLookup()) {
				availablePositions = itemHome.getAvailableShelfPosition(item.getParentItem().getItemId(), item.getuPosition(), item.getMountedRailLookup().getLkpValueCode(), item.getItemId());
			}
			else {
				availablePositions = new ArrayList<Integer>();
			}
			if (null != availablePositions) {
				int itemShelfPos = item.getShelfPosition();
				if (!availablePositions.contains(itemShelfPos) || itemShelfPos <= 0) {
					if (itemShelfPos <= 0 && item.getuPosition() != -9L) {
						Object[] errorArgs = {itemShelfPos, item.getParentItem() != null ? item.getParentItem().getItemName() : "", item.getuPosition() };
						errors.rejectValue("cmbOrder", "ItemValidator.noAvailableShelfPosition", errorArgs, "The Shelf Position is not available");
					}
				} 
				// user may enter higher value for shelf position than the 
				// next available. This may lead to gaps and should not be allowed
				// eg. shelf position 1,2, 3, 4,  v/x 1, 3, 6, 10 (which is incorrect)
				if (availablePositions.size() > 0) {
					// the collection is reverse sorted and it shows next available slot position 
					// in the first element.
					Integer pos = availablePositions.iterator().next();
					if (pos < itemShelfPos) {
						String availablePosStr = getAvailablePosStr(availablePositions);
						Object[] errorArgs = {itemShelfPos, item.getParentItem() != null ? item.getParentItem().getItemName() : "", item.getuPosition(), availablePosStr };
						errors.rejectValue("cmbOrder", "ItemValidator.InvalidShelfPosition", errorArgs, "The Shelf Position is not available");
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void validateRailsUsed(Object target, Errors errors) {
		Item item = (Item)target;
		// Do not validate Rails if the parent (Cabinet) is not provided
		if (null == item.getParentItem()) {
			return;
		}
		if (item.getMountedRailLookup() != null && item.getMountedRailLookup().getLkpValueCode() != SystemLookup.RailsUsed.FRONT && 
				item.getMountedRailLookup().getLkpValueCode() != SystemLookup.RailsUsed.REAR) {
			Object[] errorArgs = {item.getMountedRailLookup().getLkpValue(), item.getClassLookup().getLkpValue() };
			errors.rejectValue("cabinet", "ItemValidator.incorrectRailsUsed", errorArgs, "The selected Rails Used is not allowed");
		}
	}
	
	private String getAvailablePosStr (Collection<Integer> availablePositions) {
		String shelfPositionStr = "" ;
		for (Integer pos: availablePositions) {
	        if (shelfPositionStr.isEmpty()) {
	        	shelfPositionStr = shelfPositionStr + pos.toString();
	        } else {
	        	shelfPositionStr = shelfPositionStr + "," + pos.toString();
	        }
	    }
		return shelfPositionStr;
	}

}
