/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;

/**
 * @author prasanna
 *
 */
public class OutletItemUPositionValidator extends ItemUPositionValidatorBase {

	@Override
	protected void validateUPosition(Object target, Errors errors) {
		Item item = (Item)target;
		Collection<Long> availablePositions = null;
		
		availablePositions = new ArrayList<Long>();
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
	}

}
