/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.port.home.IPortObjectCollection;
import com.raritan.tdz.port.home.PortObjectCollectionFactory;

/**
 * @author prasanna
 *
 */
public class PowerPortValidator implements Validator {
	@Autowired
	private PortObjectCollectionFactory portObjectsFactory;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return Item.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		Item item = (Item) targetMap.get(errors.getObjectName());
		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
		IPortObjectCollection powerPortObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, "PowerPorts", item, errors);
		
		powerPortObjects.validate(errors);
		
	}

}
