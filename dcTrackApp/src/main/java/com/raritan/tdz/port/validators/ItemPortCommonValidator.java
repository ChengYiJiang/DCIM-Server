/**
 * 
 */
package com.raritan.tdz.port.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.validators.ItemObjectSaveValidatorsFactory;
import com.raritan.tdz.port.home.PortObjectFactory;

/**
 * @author prasanna
 *  <p>This expects a map that contains the following<p>
 *  <ul>
 *  <li>"itemId", Long value</li>
 *  <li>"portId", Long value</li>
 *  <li>"UserInfo", UserInfo</li>
 *  </ul>
 */
public class ItemPortCommonValidator<T> implements Validator {

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	PortObjectFactory portObjectFactory;

	@Autowired
	ItemObjectSaveValidatorsFactory saveValidatorFactory;
	
	
	public ItemPortCommonValidator(){
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(IPortInfo.class);
	}

		
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {

		if (target == null || !(target instanceof Map)) 
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		if (targetMap.size() < 3)
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator." +
												" At least two entries must be present");
		
		if (!targetMap.containsKey("itemId")){
			throw new IllegalArgumentException("You must provide itemId");
		}
		
		if (!targetMap.containsKey("UserInfo")){
			throw new IllegalArgumentException("You must provide userInfo");
		}
		
		if (!targetMap.containsKey("portClass")){
			throw new IllegalArgumentException("You must provide portClass");
		}
		
		if (errors == null)
		{
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
		
		if (targetMap.get("itemId") == null){
			errors.reject("PortValidator.itemIdNotProvided", null, null);
		}
		
		if (targetMap.get("UserInfo") == null){
			errors.reject("PortValidator.portIdNotProvided", null, null);
		}
		if( targetMap.get("portClass") == null ){
			errors.reject("PortValidator.portIdNotProvided", null, null);
		}
		
		if (errors.hasErrors()) return;
		
		Long itemId = (Long) targetMap.get("itemId");
		Item item = itemDAO.read(itemId);
		
		if( item == null ){
			errors.reject("PortValidator.itemNotFound", null, null);
			return;
		}
		// Check if portClass is supported for item type
		Class<?> portClazz = (Class<?>)targetMap.get("portClass");
	
		if( ! isSupported( item, portClazz ) ){
			Object[] errorArgs = {};
			errors.rejectValue("Ports", "PortValidator.dataPortUnsupportedClass", errorArgs, 
					"Port cannot be created for the selected make and model.");
		}
	}

	/*
	 * FIXME: Hacks for DataPanel and FreeStanding Dev/Net
	 * DataPannel are not supported by web client, but we have to laod ports (otherwise they will be delete) when processed
	 * over web. When processed over REST we do not treat them as supported. Hence this Hack that will go away when
	 * DataPanels are moved to web
	 * For Free standing another hack. They are always supported. However, we cannot call support() method on validator
	 * since on that level DataPorts are not supported an it has to go deeper in hiararchy to check it on cabinet and item.
	 */
	private boolean isSupported(Item item, Class<?>portClazz) {
	
		boolean supported = false;
		boolean iKnowIt = false;

		final long RACKABLE_DATA_PANNEL_UID = 301L;
		final long NON_RACKABLE_DATA_PANNEL_UID = 302L;
		final long ZERO_DATA_PANNEL_UID = 306L;
		final long FREE_STANDING_DEV = 103L;
		final long FREE_STANDING_NET = 203L;

		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
		
		if( portClazz.equals(DataPort.class) && classMountingFormFactorValue != null ){
			if(classMountingFormFactorValue.longValue() == RACKABLE_DATA_PANNEL_UID || 
					classMountingFormFactorValue.longValue() ==  NON_RACKABLE_DATA_PANNEL_UID ||
					classMountingFormFactorValue.longValue() == ZERO_DATA_PANNEL_UID){ 
				supported = false;
				iKnowIt = true;
			}else if(classMountingFormFactorValue.longValue() == FREE_STANDING_DEV ||
					classMountingFormFactorValue.longValue() == FREE_STANDING_NET){
				supported = true;
				iKnowIt = true;
			}
		}
		if( !iKnowIt ){
			Validator validator = saveValidatorFactory.getValidators(item);
			if (validator != null)
				supported = validator.supports(portClazz);
		}
		return supported;
	}

}
