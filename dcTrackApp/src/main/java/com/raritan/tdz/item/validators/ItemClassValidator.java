package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;


public class ItemClassValidator implements Validator {
	private final Logger log = Logger.getLogger(this.getClass());

	private List<Long> supportedClasses;
	
	public List<Long> getSupportedClasses() {
		return supportedClasses;
	}

	public void setSupportedClasses(List<Long> supportedClasses) {
		this.supportedClasses = supportedClasses;
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		Map<String, Object> paramsMap = (Map<String, Object>)target;
		
		Item item = (Item)paramsMap.get("item");
		Object[] errorArgs = (Object[]) paramsMap.get("errorArgs");
		String errorCode = (String)paramsMap.get("errorCodes");
		String defaultMsg = (String)paramsMap.get("defaultMessage");
		
		//must set up
		assert( errorCode != null );
		assert( errorArgs != null );
		assert( defaultMsg != null );
		
		if( item != null ) {
			boolean isSupported = false;
		
			Long uniqClassMountingFFVal = item.getClassMountingFormFactorValue();
			for( Long l : supportedClasses ){
				if( l.longValue() == uniqClassMountingFFVal.longValue()){
					isSupported = true;
					break;
				}
			}
			if( !isSupported){
				log.error("Not supported item class");
				errors.reject(errorCode, errorArgs, defaultMsg);
			}
		}
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Item.class.equals(clazz);
	}	
}
