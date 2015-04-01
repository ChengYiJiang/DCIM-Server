package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.home.InvalidPortObjectException;

/**
 * validate the object against the allowed field for the item class
 * @author bunty
 */
public class ValidateImportFieldAgainstItemClass implements Validator {

	private Map<String, List<Long>> fieldSupportedItemClass;
	
	private Map<String, String> fieldUserFriendlyName;
	
	@Autowired
	private ItemDAO itemDAO;

	public Map<String, List<Long>> getFieldSupportedItemClass() {
		return fieldSupportedItemClass;
	}

	public void setFieldSupportedItemClass(
			Map<String, List<Long>> fieldSupportedItemClass) {
		this.fieldSupportedItemClass = fieldSupportedItemClass;
	}

	public Map<String, String> getFieldUserFriendlyName() {
		return fieldUserFriendlyName;
	}

	public void setFieldUserFriendlyName(
			Map<String, String> fieldUserFriendlyName) {
		this.fieldUserFriendlyName = fieldUserFriendlyName;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		if (null == target) return;
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String, Object>) target;
		
		Long itemId = (Long) targetMap.get("itemId");
		
		if (null == itemId) return;
		
		Item item = itemDAO.loadItem(itemId);
		
		Object object = targetMap.get("objectUnderTest");
		
		if (null == object) return;
		
		for (Map.Entry<String, List<Long>> entry: fieldSupportedItemClass.entrySet()) {
			
			String field = entry.getKey();
			
			Object value = getValue(object, field);
			
			if (null == value) continue;
			
			if (value instanceof String && ((String)value).isEmpty()) continue;
			
			List<Long> supportedItemClass = entry.getValue();
			
			if (supportedItemClass.contains(item.getClassMountingFormFactorValue())) continue;
			String userFriendlyFieldName = fieldUserFriendlyName.get(field); 
			Object[] errorArgs = { item.getClassLookup().getLkpValue(), userFriendlyFieldName };
			errors.reject("Import.Item.unsupportedField", errorArgs, "Item class " + item.getClassLookup().getLkpValue() + " do not support " + userFriendlyFieldName);
			
		}

	}
	
	
	private Object getValue(Object object, String fieldName) {
		Object value = null;
		try {
			value = PropertyUtils.getProperty(object, fieldName);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for " + fieldName + ": Internal Error");
		}
		return value;
	}


}
