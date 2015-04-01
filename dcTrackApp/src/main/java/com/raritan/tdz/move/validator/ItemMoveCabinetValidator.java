package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.home.SavedItemData;

public class ItemMoveCabinetValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {

		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item) targetMap.get(errors.getObjectName());
		
		if (isCabinetChanged(item)) {
			Object[] errorArgs = { item.getItemName() };
			errors.rejectValue("cmbCabinet", "ItemMoveValidator.MoveItemCabinetChanged", errorArgs, "Move Item Cabinet change?");

		}

	}
	
	private boolean isCabinetChanged( Item item ) {
		
		if (item.getSkipValidation() != null && item.getSkipValidation()) return false;
		
		if (item.getItemId() <= 0) return false;
		
		if (null == item.getItemToMoveId()) return false;
		
		SavedItemData savedData = SavedItemData.getCurrentItem();
		Item savedItem = (null != savedData) ? savedData.getSavedItem() : null;
		
		Item cabinetToSave = item.getParentItem();
		Item savedCabinet = savedItem.getParentItem();
		
		if (null == cabinetToSave && null == savedCabinet) return false;
		
		if (null == cabinetToSave || null == savedCabinet) return true;

		Long cabinetToSaveId = cabinetToSave.getItemId();
		Long savedCabinetId = savedCabinet.getItemId();
		
		if (cabinetToSaveId.equals(savedCabinetId)) return false;
		
		return true;
		
	}

}
