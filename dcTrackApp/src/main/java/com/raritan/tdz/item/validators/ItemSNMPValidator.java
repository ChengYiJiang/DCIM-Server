package com.raritan.tdz.item.validators;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemSNMP;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * validate the item's SNMP data 
 * @author bunty
 *
 */
public class ItemSNMPValidator implements Validator {

	final int MAX_STRING_LENGTH = 64;
	final int MIN_PASSCODE_LENGTH = 8;
	
	// Class that supports snmp
	final List<Long> supportedClass = Arrays.asList( 
			SystemLookup.Class.CRAC, 
			SystemLookup.Class.CRAC_GROUP, 
			SystemLookup.Class.DEVICE, 
			SystemLookup.Class.FLOOR_PDU, 
			SystemLookup.Class.NETWORK, 
			SystemLookup.Class.PROBE, 
			SystemLookup.Class.RACK_PDU,
			SystemLookup.Class.UPS, 
			SystemLookup.Class.UPS_BANK);
	
	// allowable auth level
	final List<String> authLevel = Arrays.asList("noAuthNoPriv", "authNoPriv", "authPriv");
	
	//allowable auth protocol
	final List<String> authProtocol = Arrays.asList("MD5", "SHA");
	
	// allowable provacy protocol
	final List<String> privacyProtocol = Arrays.asList("DES", "AES");
	
	@Override
	public boolean supports(Class<?> clazz) {
		
		return clazz.getSuperclass().equals(Item.class);
	}

	@Override
	public void validate(Object target, Errors errors) {

		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		if (targetMap == null) return;
		
		Item item = (Item)targetMap.get(errors.getObjectName());
		if (item == null) return;
		
		ItemSNMP itemSnmp = item.getItemSnmp();
		if (null == itemSnmp) return;
		
		// check supported class, if item class is not supported, no need to validate data
		if (!validateSupportClass(item, errors)) return;
		
		// check values

		// NOTE: SNMP community string length is validated in the data port validator
		
		// check PX user name length
		if (null != itemSnmp.getPxUserName() && itemSnmp.getPxUserName().length() > MAX_STRING_LENGTH) {
			Object args[]  = { MAX_STRING_LENGTH };
			String code = "ItemValidator.SNMP.InvalidPxNameLength";
			errors.reject(code, args, "PX username cannot exceed 64 characters");
			
		}
		
		// check PX password length
		if (null != itemSnmp.getPxPassword() && itemSnmp.getPxPassword().length() > MAX_STRING_LENGTH) {
			Object args[]  = { MAX_STRING_LENGTH };
			String code = "ItemValidator.SNMP.InvalidPxPasswordLength";
			errors.reject(code, args, "PX password cannot exceed 64 characters");
						
		}
				
		// V3 disabled, no need to check other parameters, it should be cleared by the snmp behavior
		if (null == itemSnmp.getSnmp3Enabled() || !itemSnmp.getSnmp3Enabled()) return;
		
		// check if user name is provided if v3 is enabled
		if (null == itemSnmp.getSnmp3User() || itemSnmp.getSnmp3User().length() == 0) {
			String code = "ItemValidator.SNMP.V3.UserNameRequired";
			errors.reject(code, "The SNMP v3 Username is required.");
		}
		
		// check user name length
		if (null != itemSnmp.getSnmp3User() && itemSnmp.getSnmp3User().length() > MAX_STRING_LENGTH) {
			Object args[]  = { MAX_STRING_LENGTH };
			String code = "ItemValidator.SNMP.InvalidNameLength";
			errors.reject(code, args, "SNMP V3 username cannot exceed 64 characters");
		}

		// V3 enabled, check auth level values
		if (null == itemSnmp.getSnmp3AuthLevel() || !authLevel.contains(itemSnmp.getSnmp3AuthLevel())) {
			String code = "ItemValidator.SNMP.InvalidAuthLevel";
			errors.reject(code, "SNMP V3 Authorization Level is invalid. Supported values are 'noAuthNoPriv', 'authNoPriv', 'authPriv'");
			return;
		}
		
		// V3 enabled, auth level is 'noAuthNoPriv'. Validate auth protocol: 'noAuthNoPriv'. do not check other parameters
		if (itemSnmp.getSnmp3AuthLevel().equals("noAuthNoPriv")) return;
		
		// V3 enabled, auth level is 'auth*'. Validate protocol 
		if (itemSnmp.getSnmp3AuthLevel().equals("authNoPriv") || itemSnmp.getSnmp3AuthLevel().equals("authPriv")) {
			
			// validate auth protocol
			if (null == itemSnmp.getSnmp3AuthProtocol() || !authProtocol.contains(itemSnmp.getSnmp3AuthProtocol())) {
				String code = "ItemValidator.SNMP.InvalidAuthProtocol";
				errors.reject(code, "SNMP V3 Authorization Protocol is invalid. Supported values are 'SHA', 'MD5'");
				
			}
			
			// validate auth pass key length
			if (null == itemSnmp.getSnmp3AuthPasskey() || 
					itemSnmp.getSnmp3AuthPasskey().length() < MIN_PASSCODE_LENGTH || itemSnmp.getSnmp3AuthPasskey().length() > MAX_STRING_LENGTH) {
				Object args[]  = { MIN_PASSCODE_LENGTH, MAX_STRING_LENGTH };
				String code = "ItemValidator.SNMP.InvalidAuthPassKeyLength";
				errors.reject(code, args, "SNMP V3 Authorization Passkey must be between 8 to 64 characters");
				
			}
			
		}

		// V3 enabled, auth level is 'authPriv'. Validate privacy
		if (itemSnmp.getSnmp3AuthLevel().equals("authPriv")) {
			
			// check privacy protocol
			if (null == itemSnmp.getSnmp3PrivProtocol() || !privacyProtocol.contains(itemSnmp.getSnmp3PrivProtocol())) {
				String code = "ItemValidator.SNMP.InvalidPrivacyProtocol";
				errors.reject(code, "SNMP V3 Privacy Protocol is invalid. Supported values are 'AES', 'DES'");
				
			}
			
			// check privacy passkey length
			if (null == itemSnmp.getSnmp3PrivPasskey() || 
					itemSnmp.getSnmp3PrivPasskey().length() < MIN_PASSCODE_LENGTH || itemSnmp.getSnmp3PrivPasskey().length() > MAX_STRING_LENGTH) {
				Object args[]  = { MIN_PASSCODE_LENGTH, MAX_STRING_LENGTH };
				String code = "ItemValidator.SNMP.InvalidPrivPassKeyLength";
				errors.reject(code, args, "SNMP V3 Privacy Passkey must be between 8 to 64 characters");
				
			}
			
		}

	}
	
	
	
	public static boolean isGetter(Method method) {
		
		if (Modifier.isPublic(method.getModifiers()) &&
				method.getParameterTypes().length == 0) {
			
			if (method.getName().matches("^get[A-Z].*") &&
		            !method.getReturnType().equals(void.class))
				return true;
			
			if (method.getName().matches("^is[A-Z].*") &&
					method.getReturnType().equals(boolean.class))
				return true;
			
	   }
		
	   return false;
	   
	}
	
	private boolean validateSupportClass(Item item, Errors errors) {

		ItemSNMP itemSnmp = item.getItemSnmp();
		
		Long itemClass = item.getClassLookup().getLkpValueCode();
		
		if (!supportedClass.contains(itemClass)) {
			
			boolean allValueNull = true;
			Method[] methods = itemSnmp.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if (isGetter(method)) {
			    	 try {
						Object retVal = method.invoke(itemSnmp, (Object[])null);
						if (null != retVal && !(retVal instanceof Item)) {
							allValueNull = false;
							break;
						}
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						allValueNull = true;
					} 
				}
			}
			
			if (!allValueNull) {
				String code = "ItemValidator.SNMP.NotSupportedClass";
				errors.reject(code, "Item class do not support snmp");
			}
			return false;
		}

		return true;
	}

}
