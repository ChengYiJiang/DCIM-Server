package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

public class ItemMoveSupportedItemValidator implements Validator {

	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {

		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item) targetMap.get(errors.getObjectName());
		
		if (null == item.getItemToMoveId()) return;

		if (!isSupportedItemClass(item)) { 
			Object[] errorArgs = { item.getItemName() };
			errors.rejectValue("tiClass", "ItemMoveValidator.ItemClassSupportedForMove", errorArgs, "Item class do not support move.");
		}
		
		if (!isSupportedMounting(item)) { 
			Object[] errorArgs = { item.getItemName() };
			errors.rejectValue("tiMounting", "ItemMoveValidator.ItemMountingSupportedForMove", errorArgs, "Mounting do not support move.");
		}

		if (!isSupportedStatus(item)) { 
			Object[] errorArgs = { item.getItemName() };
			errors.rejectValue("tiMounting", "ItemMoveValidator.ItemStatusSupportedForMove", errorArgs, "Item Status do not support move.");
		}

	}
	
	private boolean isSupportedItemClass(Item item) {
		
		LksData classLks = item.getClassLookup();
		
		if (null == classLks) return false;
		
		Long classLkpValueCode = classLks.getLkpValueCode();
		
		if (null == classLkpValueCode) return false;
		
		LksData subClassLks = item.getSubclassLookup();
		
		ModelDetails model = item.getModel();
		
		if (null == model) return false;
		
		String mounting = model.getMounting();
		
		if (
				(classLkpValueCode.equals(SystemLookup.Class.DEVICE) && !mounting.equals(SystemLookup.Mounting.FREE_STANDING)) || // free-standing device not allowed 
				(classLkpValueCode.equals(SystemLookup.Class.NETWORK) && !mounting.equals(SystemLookup.Mounting.FREE_STANDING))|| // free-standing network not allowed
				(classLkpValueCode.equals(SystemLookup.Class.CABINET) && null == subClassLks) // container cabinet not allowed
			)  
			return true;

		return false;
		
	}
	
	private boolean isSupportedMounting(Item item) {
		
		ModelDetails model = item.getModel();
		
		if (null == model) return false;
		
		String mounting = model.getMounting();
		
		LksData classLks = item.getClassLookup();
		
		if (null == classLks) return false;
		
		Long classLkpValueCode = classLks.getLkpValueCode();
		
		if (null == classLkpValueCode) return false;
		
		if ((null == mounting || !(mounting.equals(SystemLookup.Mounting.RACKABLE) || 
				mounting.equals(SystemLookup.Mounting.FREE_STANDING) ||
				mounting.equals(SystemLookup.Mounting.BLADE) ) ) ) 
			return false;
		
		return true;
		
	}
	
	private boolean isSupportedStatus(Item item) {

		Long movingItemId = item.getItemToMoveId();
		
		Item movingItem = itemDAO.getItem(movingItemId);
		
		if (null == movingItem) return false;
		
		LksData statusLks = movingItem.getStatusLookup();
		
		if (null == statusLks) return false;
		
		Long statusLkpValueCode = statusLks.getLkpValueCode();
		
		if (null == statusLkpValueCode) return false;
		
		if (statusLkpValueCode.equals(SystemLookup.ItemStatus.INSTALLED) || statusLkpValueCode.equals(SystemLookup.ItemStatus.POWERED_OFF)) return true;

		return false;
		
	}

}
