/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class NumberInGroupValidator implements Validator {

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return MeItem.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Object itemDomainObject = targetMap.get(errors.getObjectName());
		validateNumberInGroup(itemDomainObject,errors);
	}
	
	/*
	 * Validate the number in group is in the range 0-9999. (US1409)
	 * 
	 * NOTE: Unfortunately we cannot use bean annotation validation for the number range
	 * (using the @Max annotation) because the "num_ports" field is overloaded in the database.
	 */
	private void validateNumberInGroup(Object target, Errors errors) {
		if (target == null || errors == null) return;
		final Item item = (Item)target;
		
		// Only applies to Rack PDU?
		if (item.getClassLookup() == null || item.getClassLookup().getLkpValueCode() != SystemLookup.Class.RACK_PDU) 
			return;
		
		final int numInGroup = item.getNumPorts();
		
		if (numInGroup < 0 || numInGroup > 9999) {
			Object[] errorArgs = { };
			errors.rejectValue("numPorts", "ItemValidator.invalidNumberInGroup", errorArgs, "The Number in Group field must be in the range 0-9999");
		}
	}

}
