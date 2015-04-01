/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.port.home.InvalidPortObjectException;

/**
 * @author prasanna
 *
 */
public class SupportedPortClassValidator implements Validator {

	//This is a map of the all the port classes to a boolean indicating if this port is supported or not.
	Map<String,Boolean> supportedPortClassMap;
	
	public SupportedPortClassValidator(
			Map<String, Boolean> supportedPortClassMap) {
		this.supportedPortClassMap = supportedPortClassMap;
	}

	/**
	 * 
	 * clazz - Port class (e.g. DataPort, PowerPort, SensorPort)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		boolean supported = false;
	
		for (Map.Entry<String, Boolean> entry : supportedPortClassMap.entrySet()){
			String supportedClass = entry.getKey();
			Boolean value = entry.getValue();
			
			String[] clazzNameArr = (clazz != null) ? clazz.getCanonicalName().split("\\.") : null;
			if(clazzNameArr != null &&  clazzNameArr.length > 0 && supportedClass.equals(clazzNameArr[clazzNameArr.length-1]) ){
			//if( clazz.getCanonicalName().equals(supportedClass)){
				if( value == true ){
					supported = true;
					break;
				}
			}
		}	
		return supported;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Object itemDomainObject = targetMap.get(errors.getObjectName());
		
		Item item = (Item) itemDomainObject;
		
		for (Map.Entry<String, Boolean> entry : supportedPortClassMap.entrySet()){
			
			String supportedClass = entry.getKey();
			Boolean value = entry.getValue();
			
			String supported = Character.toLowerCase(
					supportedClass.charAt(0)) + (supportedClass.length() > 1 ? supportedClass.substring(1) : "") + "s";
			
			Set<?> ports = (Set<?>) getValue(item,supported);
			
			if (ports != null && !ports.isEmpty() && value == false){
				String field = "tab" + supportedClass;
				String errorCode = getPortClassErrorCode(supportedClass);
				String itemClass = item.getClassLookup() != null ? item.getClassLookup().getLkpValue() : "for this type of item";
				Object[] errorArgs = {itemClass};
				errors.rejectValue(field, errorCode,errorArgs,"Unsupported Port Class");
			}
		}

	}

	private String getPortClassErrorCode(String portClass) {
		StringBuffer code = new StringBuffer();
		
		
		code.append("PortValidator.");
		code.append(Character.toLowerCase(
				portClass.charAt(0)) + (portClass.length() > 1 ? portClass.substring(1) : ""));
		code.append("UnsupportedClass");
		return code.toString();
	}
	
	private Object getValue(Object port, String fieldName) {
		Object value = null;
		try {
			value = PropertyUtils.getProperty(port, fieldName);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for " + fieldName + ": Internal Error");
		}
		return value;
	}
}
