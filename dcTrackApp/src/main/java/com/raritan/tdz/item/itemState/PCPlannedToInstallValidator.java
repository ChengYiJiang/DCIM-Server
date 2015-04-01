/**
 * 
 */
package com.raritan.tdz.item.itemState;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class PCPlannedToInstallValidator implements
		ParentChildConstraintValidator {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.ParentChildConstraintValidator#validateParentChildConstraint(com.raritan.tdz.domain.Item, java.lang.Long, org.springframework.validation.Errors)
	 */
	@Override
	public void validateParentChildConstraint(Item item,
			Long newStatusLkpValueCode, Errors errors, String errorCodePrefix)
			throws DataAccessException {
		
		String itemName = item != null ? item.getItemName():"<Unknown>";
		Item parentItem = item.getParentItem();
		if (parentItem != null 
				&& parentItem.getStatusLookup() != null 
				&& !parentItem.getStatusLookup().getLkpValueCode().equals(SystemLookup.ItemStatus.INSTALLED)){
			String parentItemName = parentItem.getItemName();
			String parentClassValue = parentItem.getClassLookup() != null ? parentItem.getClassLookup().getLkpValue() : "<Unknown>";
			//The parent is not installed and we should not allow the child to be in installed state.
			Object errorArgs[] = { itemName, parentItemName, parentClassValue };
			errors.reject(errorCodePrefix + ".installRequest", errorArgs, "Cannot install item");
		}
		
		if (item instanceof ItItem){
			ItItem itItem = (ItItem) item;
			
			Item chassisItem = itItem.getBladeChassis();
			
			if (chassisItem != null 
					&& chassisItem.getStatusLookup() != null 
					&& !chassisItem.getStatusLookup().getLkpValueCode().equals(SystemLookup.ItemStatus.INSTALLED)){
				String parentItemName = chassisItem.getItemName();
				String parentClassValue = chassisItem.getClassLookup() != null ? chassisItem.getClassLookup().getLkpValue() : "<Unknown>";
				//The parent is not installed and we should not allow the child to be in installed state.
				Object errorArgs[] = { itemName, parentItemName, parentClassValue };
				errors.reject(errorCodePrefix + ".installRequest", errorArgs, "Cannot install item");
			}
			
		}
	}

}
