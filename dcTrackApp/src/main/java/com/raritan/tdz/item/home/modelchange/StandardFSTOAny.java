/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 * This will take care of any logic that is common to all the standardFS to any type of an item
 */
public abstract class StandardFSTOAny implements ChangeModel {
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModel#init(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item, java.lang.Object)
	 */
	@Override
	public void init(Item itemInDB, Item itemToSave, Object additionalParams) {
		//We for sure know that itemInDB should be a FreeStanding. 
		//We will check if the itemToSave is also not a free standing device/network item. If so we do not want to initialize anything
		//Then we will check if the parentItem are the same for both itemInDB to itemToSave in addition to the above check.
		//If they are then itemToSave is definitely some other type of item and we need to make sure we initialize the parent as the new item cannot have
		//phantom cabinet!
		if (itemToSave != null && itemInDB != null 
				&& (!itemToSave.getClassMountingFormFactorValue().equals(SystemLookup.ModelUniqueValue.DeviceStandardFreeStanding)
						|| !itemToSave.getClassMountingFormFactorValue().equals(SystemLookup.ModelUniqueValue.NetworkStackFreeStanding))
				&& itemInDB.getParentItem() != null && itemToSave.getParentItem() != null
				&& itemInDB.getParentItem().getItemId() == itemToSave.getParentItem().getItemId() ){
				itemToSave.setParentItem(null);
		}
	}

}
