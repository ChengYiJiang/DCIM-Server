/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.SavedItemData;

/**
 * @author prasanna
 *
 */
public class CabinetLicenseValidator implements Validator {

	
	@Autowired
	private ItemHome itemHome;
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(CabinetItem.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>) target;
		
		Item cabinet = (Item) targetMap.get(errors.getObjectName());
		
		//ItemId <= 0 means this a new cabinet, cabinet.getItemToMoveId() > 0 means this is a -when-moved cabinet
		if (cabinet.getItemId() <= 0 && (null == cabinet.getItemToMoveId() || cabinet.getItemToMoveId() <= 0)) {
			if (!itemHome.isLicenseAvailable(1)){
				Integer licenses = itemHome.getLicenseCount();
				Object[] errorArgs = {licenses};
				errors.reject("ItemValidator.licenseExceeded", errorArgs, "You are exceeding the current license limit");
			}
		}
	}
}
