/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.SavedItemData;

/**
 * @author prasanna
 *
 */
public class NRItemUPositionValidator extends ItemUPositionValidatorBase {

	@Autowired
	ItemHome itemHome;

	@Override
	protected void validateUPosition(Object target, Errors errors) {
		Item item = (Item)target;
		Collection<Long> availablePositions = null;
		// Do not validate U position if the parent (Cabinet) is not provided
		if (null == item.getParentItem()) {
			return;
		}
		Item origItem = (null != SavedItemData.getCurrentItem()) ? SavedItemData.getCurrentItem().getSavedItem() : null;
		try {
			
			if (null != item.getParentItem() && null != item.getModel() && null != item.getMountedRailLookup()) {
				availablePositions = itemHome.getNonRackableUPosition(item.getParentItem().getItemId(), 
					item.getModel().getModelDetailId(), item.getMountedRailLookup().getLkpValueCode(), (null != origItem) ? origItem.getItemId() : -1);
			}
			else {
				availablePositions = new ArrayList<Long>();
			}

			// Add positions that always available
			availablePositions.add( -1L ); // Above Cabinet
			availablePositions.add( -2L ); // Below Cabinet
			availablePositions.add( -9L ); // No U-position selected
			if (null != availablePositions) {
				if (!availablePositions.contains(item.getuPosition())) {
					Object[] errorArgs = {item.getuPosition(), item.getParentItem() != null ? item.getParentItem().getItemName() : "<Unknown>" };
					errors.rejectValue("cmbCabinet", "ItemValidator.noAvailableUPosition", errorArgs, "The UPosition is not available");
				}
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
