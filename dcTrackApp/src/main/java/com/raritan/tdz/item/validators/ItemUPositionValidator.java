/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.UiIdLookup;

/**
 * @author prasanna
 *
 */
public class ItemUPositionValidator extends ItemUPositionValidatorBase {
	
	@Autowired
	protected ItemPlacementHome itemPlacementHome;
	
	@Override
	protected void validateUPosition(Object target, Errors errors) {
		Item item = (Item)target;
		try {
			isItemPlacementValid(item, errors);
		} catch (DataAccessException e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}
	
	/*
	 * Validate that the placement is still valid if the model RU Height changes on an existing item.
	 */
	public boolean isItemPlacementValid(Item item, Errors errors) throws DataAccessException {
		if (null == item) return true;
		if (item.getParentItem() == null ) {
			// error when user provides uPosition without cabinet
			if (item.getuPosition() != 0 && item.getuPosition() != SystemLookup.SpecialUPositions.NOPOS) {
				errors.rejectValue(UiIdLookup.UiId.CMB_CABINET, "ItemValidator.cabinetNotSelected", "The U Position is not valid, no cabinet selected.");
				return false;
			}
			return true;
		}
		
		if (item.getModel() != null && 
				item.getModel().getMounting().equals(SystemLookup.Mounting.ZERO_U)) {
			long uPosition = item.getUPosition();
			if (uPosition == SystemLookup.SpecialUPositions.ABOVE || 
					uPosition== SystemLookup.SpecialUPositions.BELOW){
				errors.rejectValue(UiIdLookup.UiId.CMB_CABINET, "ItemValidator.InvalidUPositionForZeroUItem",  "The U Position is not valid");
				return false;
			}
		}
		
		if (item.getParentItem().isStatusArchived()) {
			// skip the validation for the container subclass item, the free-standing item and cabinet will change the status together: US2223
			if (item.getParentItem().getSubclassLookup() == null || 
					(item.getParentItem().getSubclassLookup() != null && item.getParentItem().getSubclassLookup().getLkpValueCode().longValue() != SystemLookup.SubClass.CONTAINER)) {
				String parentType = (null != item.getParentItem().getClassLookup() && item.getParentItem().getClassLookup().getLkpValueCode() == SystemLookup.Class.CABINET) ? "Cabinet" : 
											((null != item.getParentItem().getSubclassLookup() && (item.getParentItem().getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.CHASSIS || 
													item.getParentItem().getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.BLADE_CHASSIS)) ? "Chassis" : ""); 
				Object[] errorArgs = { parentType, item.getParentItem().getItemName() };
				errors.rejectValue(UiIdLookup.UiId.CMB_CABINET, "ItemValidator.invalidParentStatus", errorArgs, "Cabinet / Chassis status do not allow placement");
				return false;
			}
		}
		final long uPosition = item.getuPosition();
		
		// user didn't specify uPosition - it was defaulted to 0 while adapting ValueIdDTO to bean in convert 
		if (uPosition == 0) {
			errors.rejectValue(UiIdLookup.UiId.CMB_CABINET, "ItemValidator.UPositionNotValid",  "The U Position is not valid");
			return false;
		}
		Collection<Long> availPositions = getAvailableUPositions( item ); 
		if ( availPositions != null ) {
			availPositions.add( -1L ); // Above Cabinet
			availPositions.add( -2L ); // Below Cabinet
		}
		
		if (item.getClassLookup() !=null && !(item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.PASSIVE))) {
			availPositions.add( -9L ); // No U Position selected
		}
		
		if (!availPositions.contains(uPosition)) {
			if (uPosition == SystemLookup.SpecialUPositions.NOPOS) {
				errors.rejectValue(UiIdLookup.UiId.CMB_CABINET, "ItemValidator.UPositionRequired", "Select valid U Position");
			} else {
				Object[] errorArgs = {item.getuPosition(), item.getParentItem().getItemName() };
				errors.rejectValue(UiIdLookup.UiId.CMB_CABINET, "ItemValidator.noAvailableUPosition", errorArgs, "The U Position is not available");
			}
			return false;
		}
		return true;
	}
	
	private Collection<Long> getAvailableUPositions( Item item ) throws DataAccessException {
		Collection<Long> availablePositions = null;
		
		Item origItem = null;
		if (item.getItemId() > 0) {
			SavedItemData savedData = SavedItemData.getCurrentItem();
			origItem = (null != savedData) ? savedData.getSavedItem() : null;
		}
		availablePositions = itemPlacementHome.getAvailablePositions( item, origItem );
		
		return availablePositions;
	}
}
