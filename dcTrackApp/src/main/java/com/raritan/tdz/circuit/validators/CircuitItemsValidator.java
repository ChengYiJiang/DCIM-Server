
/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author  Santo Rosario
 *
 */
public class CircuitItemsValidator implements Validator {
	
	@Autowired
	ItemDAO itemDAO;

	public CircuitItemsValidator() {
		super();
	}
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Item.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		Item item = (Item)target;				
		item = itemDAO.getItem(item.getItemId());
		
		validateStatus(item, errors);
		validatePlacement(item, errors);
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide an item target");
	}
	
	private void validateStatus(Item item, Errors errors) {
		if(item.isStatusArchived() || item.isStatusStorage()) {
			Object errorArgs[] = new Object[] { item.getItemName(), item.getStatusLookup().getLkpValue() };
			errors.reject("Import.Circuit.InvalidItemStatus", errorArgs, "Invalid item status");
		}
		
	}

	private void validatePlacement(Item item, Errors errors) {
		if(item.isClassCRAC() || item.isClassFloorPDU() || item.isClassUPS() || item.isSubClassVM()) return;
		
		if(item.getParentItem() != null && (item.getUPosition() > 0 || item.getSlotPosition() > 0)) return;
		
		if(item.getParentItem() != null && (item.getUPosition() == SystemLookup.SpecialUPositions.ABOVE || item.getUPosition() == SystemLookup.SpecialUPositions.BELOW)) return;
		
		if(item.isClassFloorOutLet()) {
			if(item.getParentItem() == null && (item.getLocationReference() != null && item.getLocationReference().trim().length() > 0 )) return;
		}
		
		Object errorArgs[] = new Object[] { item.getItemName() };
		errors.reject("Import.Circuit.InvalidPlacement", errorArgs, "Invalid item placement information");			
	}	
}
