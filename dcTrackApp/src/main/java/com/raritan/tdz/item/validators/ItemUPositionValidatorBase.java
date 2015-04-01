/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public abstract class ItemUPositionValidatorBase implements Validator {

	protected Logger log = Logger.getLogger(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.getSuperclass().equals(Item.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Object itemObject = targetMap.get(errors.getObjectName());
		
		validateUPosition(itemObject,errors);
	}

	protected abstract void validateUPosition(Object target, Errors errors);
}
