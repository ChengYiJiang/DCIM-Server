package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

public class PassiveItemNameUniqueValidator implements Validator {

	@Autowired
	ItemDAO itemDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.getSuperclass().equals(Item.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Object itemObject = targetMap.get(errors.getObjectName());
		
		validatePassiveItemNameUniqueness(itemObject,errors);		
	}

	
	private void validatePassiveItemNameUniqueness(Object itemObject,Errors errors) {
		Item item = (Item) itemObject;
		
		if (item != null && 
				item.getuPosition() == SystemLookup.SpecialUPositions.ABOVE || 
				item.getuPosition() == SystemLookup.SpecialUPositions.BELOW) {
			boolean itemNameExists = itemDAO.doesItemWithNameExistsAboveOrBelowCabinet(
									item.getItemName(), item.getParentItem().getItemId(), item.getuPosition());
			if (itemNameExists == true) {
				String itemName = item.getItemName(); 
				String uPosition = item.getuPosition() == SystemLookup.SpecialUPositions.ABOVE ? "Above": "Below";
				Object args[] = { itemName, uPosition };
				String code = "ItemValidator.ItemNameNotUniqueForPassiveItem";
				errors.reject(code, args, "Passive item with name already exists.");
				
			}
		}
	}

}
