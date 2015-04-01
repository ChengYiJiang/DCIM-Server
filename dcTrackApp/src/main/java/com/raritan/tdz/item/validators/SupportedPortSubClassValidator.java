/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.port.home.InvalidPortObjectException;

/**
 * @author prasanna
 *
 */
public class SupportedPortSubClassValidator<T> implements Validator {

	protected Class<T> type;
	
	private List<Long> supportedSubClass;

	
	public List<Long> getSupportedSubClass() {
		return supportedSubClass;
	}

	public void setSupportedSubClass(List<Long> supportedSubClass) {
		this.supportedSubClass = supportedSubClass;
	}

	public SupportedPortSubClassValidator(Class<T> type){
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		//TODO Auto-generated method stub
		//TODO Need to think on hot to verify the supports for this validator.
		return type.equals(clazz);
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
		
		validatePortSubClass(item,errors);
	}

	private void validatePortSubClass(Item item,Errors errors) {
		String fieldName = type.getSimpleName() + "s";
		fieldName = Character.toLowerCase(
				fieldName.charAt(0)) + (fieldName.length() > 1 ? fieldName.substring(1) : "");
		
		Set<T> ports = (Set<T>) getValue(item, fieldName);
		
		if (ports != null){	
			for (T port:ports){
				LksData subclassLks = (LksData) getValue(port,"portSubClassLookup");
				if (subclassLks != null){
					if (!supportedSubClass.contains(subclassLks.getLkpValueCode())){
						String errorCode = getPortSubClassErrorCode(subclassLks);
						String rejectedField = "tab" + type.getSimpleName();
						Object[] errorArgs = {getValue(port,"portName")};
						errors.rejectValue(rejectedField, errorCode,errorArgs,"Port subclass unsupported");
					}
				}
			}
		}
	}
	
	private String getPortSubClassErrorCode(LksData subclassLks) {
		StringBuffer code = new StringBuffer();
		
		String subclassLksValue = new String(subclassLks.getLkpValue());
		
		subclassLksValue = subclassLksValue.replaceAll("\\s+", "");
		
		code.append("PortValidator.");
		code.append(Character.toLowerCase(
				subclassLksValue.charAt(0)) + (subclassLksValue.length() > 1 ? subclassLksValue.substring(1) : ""));
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
