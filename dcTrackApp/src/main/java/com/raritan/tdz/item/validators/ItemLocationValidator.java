package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.UserInfo;

public class ItemLocationValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (Item.class.equals(clazz));
	}

	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		if (targetMap != null) {
			Item item = (Item)targetMap.get(errors.getObjectName());
			if (item == null) throw new IllegalArgumentException ("Item cannot be null");
	
			// The Location is mandatory field for creating an item in dcTrack. 
			// Here we check whether the item has location set Or else reports error.
			// The ItemDTOAdopter or convert() function call in ItemObjectTemplateSingle verifies
			// if user provided location can be mapped to correct location if not throws error.
			if (item.getDataCenterLocation() == null) {
				errors.rejectValue("itemLocation", "ItemValidator.noLocation", "No location is selected for this item");
			}
		} else {
			throw new IllegalArgumentException("ItemValidatorCommon: Invalid (null) arguments");
		}
	}
}
