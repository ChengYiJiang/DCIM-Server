package com.raritan.tdz.item.validators;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.Filter;
import com.raritan.tdz.rulesengine.FilterHQLImpl;
import com.raritan.tdz.rulesengine.RulesNodeEditability;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.GlobalUtils;

public class ItemValidatorEditabilty implements Validator {

	private static Logger log = Logger.getLogger("ValidateFieldEdit");
	
	private RulesProcessor itemRulesProcessor;
	
	@Autowired
	private ItemDAO itemDAO;

	/* Map of the database fields against the uiId */ 
	private Map<String, String> uiFieldToDBField;
	
	/* Map of mounting against the placement fields that are valid for a given mounting */
	private Map<String, List<String>> mountingPlacementFieldsMap;
	
	/* for a given field: when DB value is null, the allowable list of values against that */
	private Map<String, List<String>> validNullValueInDBMap;
	
	/* map of the uiId and the corresponding hibernate domain fields. 
	 * The fields will allow us to reach the corresponding value in the hibernate domain object */
	private Map<String, List<String>> uiViewToHibernateField;
	
	/* Map of the uiId and the user readable value */
	private Map<String, String> uiViewToUserReadableName;
	
	public RulesProcessor getItemRulesProcessor() {
		return itemRulesProcessor;
	}

	public void setItemRulesProcessor(RulesProcessor itemRulesProcessor) {
		this.itemRulesProcessor = itemRulesProcessor;
	}
	
	

	public ItemValidatorEditabilty(RulesProcessor itemRulesProcessor, 
			Map<String, String> uiFieldToDBField, 
			Map<String, List<String>> mountingPlacementFieldsMap, 
			Map<String, List<String>> validNullValueInDBMap, 
			Map<String, List<String>> uiViewToHibernateField, 
			Map<String, String> uiViewToUserReadableName) {

		this.itemRulesProcessor = itemRulesProcessor;
		this.uiFieldToDBField = uiFieldToDBField;
		this.mountingPlacementFieldsMap = mountingPlacementFieldsMap;
		this.validNullValueInDBMap = validNullValueInDBMap;
		this.uiViewToHibernateField = uiViewToHibernateField;
		this.uiViewToUserReadableName = uiViewToUserReadableName; // use dct_fields
		
	}

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * validate that the non-editable field value is not changed 
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;

		// Check the item and user information provided in the map
		Item item = (Item)targetMap.get(errors.getObjectName());
		if (item == null) {
			throw new IllegalArgumentException ("Item cannot be null");
		}
		UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
		if (userInfo == null) { 
			throw new IllegalArgumentException ("UserInfo cannot be null"); 
		}

		// Get item information that will be needed to filter the fields from the client that needs to be validated
		Long itemId = item.getItemId();

		try {

			// Collect the DB fields against the UI field
			Map<String, String> queryMapDBFieldToAlias = getDBFieldToUiIdMap(itemId, item);
			
			// from the sql field name get the value as a map (DB_Field, Value) using SQL query
			Map<String, Object> dbValues = itemDAO.getFieldsValue(itemId, queryMapDBFieldToAlias);

			// Compare the value in the item map against the value in the DB map
			List<String> fieldNotEqual = compareNonEditableFieldsValue(item, dbValues);
			
			// Generate the error message for the user
			generateErrorMessage(fieldNotEqual, errors);
			
		} 
		// Catch all exceptions, we do not want user to not save because of internal error
		catch (ClassNotFoundException | SecurityException
				| NoSuchFieldException | NoSuchMethodException
				| IllegalArgumentException | IllegalAccessException
				| InvocationTargetException e) {
			
			// Ignore all excpetions and let the validation be skipped. We do not want to stop user from saving item in case of internal errors.
			e.printStackTrace();
			
		}

		
	}
	
	/**
	 * get the filter map that is used to collect all non-editable fields
	 * @param itemId
	 * @return
	 */
	private Map<String, Filter> getFilterMap(Long itemId) {
		Filter filterForItem = FilterHQLImpl.createFilter();
		filterForItem.eq("itemId",itemId);

		Map<String,Filter> filterMap = new HashMap<String, Filter>();
		
		filterMap.put("main",filterForItem);
		return filterMap;
	}

	/**
	 * compare 2 numbers and provide the difference
	 * @param x
	 * @param y
	 * @return (x - y)
	 */
	private int numberCompare(final Object x, final Object y) {
	    if(isSpecial(x) || isSpecial(y))
	        return Double.compare(((Number)x).doubleValue(), ((Number)y).doubleValue());
	    else
	        return toBigDecimal(x).compareTo(toBigDecimal(y));
	}

	/**
	 * allow double and float interchangeably.
	 * check for infinite and NaN as special values for Double and Float  
	 * @param x
	 * @return
	 */
	private static boolean isSpecial(final Object x) {
	    boolean specialDouble = x instanceof Double
	            && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
	    boolean specialFloat = x instanceof Float
	            && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));
	    return specialDouble || specialFloat;
	}

	/**
	 * convert the number to BigDecimal that fits all kind of number values
	 * @param number
	 * @return
	 */
	private static BigDecimal toBigDecimal(final Object number) {
	    if(number instanceof BigDecimal)
	        return (BigDecimal) number;
	    if(number instanceof BigInteger)
	        return new BigDecimal((BigInteger) number);
	    if(number instanceof Byte || number instanceof Short
	            || number instanceof Integer || number instanceof Long)
	        return new BigDecimal(((Number)number).longValue());
	    if(number instanceof Float || number instanceof Double)
	        return new BigDecimal(((Number)number).doubleValue());
	    if ((Object)number instanceof String && GlobalUtils.isNumeric((String) number)) 
	    	return new BigDecimal((String)number);
	    return null;
	}
	
	/**
	 * get the user provided value using PropertyUtils. This API will parse through the provided fields to reach the value in hibernate
	 * @param item
	 * @param fields
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private Object getUserValue(Object item, List<String> fields) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		Object value = null;

		// return null if the field is incorrect
		if (null == item || null == fields || fields.size() == 0) return value;
		
		// get the class type
		String itemClassType = item.getClass().getName();
		
		// Copy the item class and the field list
		List<String> hibernateFields = new ArrayList<String>(fields);
		// if the class is of the base class "Item" or it is NOT of the matching class, return null 
		if (!hibernateFields.get(0).equals(Item.class.getName()) && !itemClassType.equals(hibernateFields.get(0))) return null;
		
		// Remove the class information from the list, thats the reason fields had to be copied
		hibernateFields.remove(0);
		
		// start with the base domain object as the value
		value = item;
		
		// reach till the value as expected to have the field value
		for (String field: hibernateFields) {
			if (null == value) break; 
			value = getValue(value, field);
		}
		
		return value;
	}
	
	/**
	 * return the value using PropertyUtils
	 * @param item
	 * @param fieldName
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private Object getValue(Object item, String fieldName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = null;
		value = PropertyUtils.getProperty(item, fieldName);
		return value;
	}

	/**
	 * get the map that has only non-editable fields and the corresponding uiId in a map.
	 * @param itemId
	 * @param item
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private Map<String, String> getDBFieldToUiIdMap(Long itemId, Item item) throws ClassNotFoundException, SecurityException, NoSuchFieldException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String mounting = (null != item.getModel()) ? item.getModel().getMounting() : SystemLookup.Mounting.VSTACK;
		Map<String, Filter> filterMap = getFilterMap(itemId);
		
		// Get the non-editable ui fields
		RulesNodeEditability editability = itemRulesProcessor.getEditabilityInfo("uiView[@uiId='itemView']/", filterMap,  "itemId", itemId, "=", new LongType(), "1");
		List<String> nonEditableFields = editability.getNonEditableNodes();
		
		if (log.isDebugEnabled()) log.debug("non Editable Fields Size = " + nonEditableFields.size() + "\nNon-Editable Fields: " + nonEditableFields);
		
		Pattern MY_PATTERN = Pattern.compile("\\[@uiId='(.*?)'\\]");
		
		// Collect the DB fields against the UI field
		Map<String, String> queryMapDBFieldToAlias = new HashMap<String, String>();
		for (String uiId: nonEditableFields) {
			
			Matcher matcher = MY_PATTERN.matcher(uiId);

			String field = null;
			while(matcher.find()) {
				field = matcher.group(1);
			}
			
			// get the actual sql field name for the uiId field value
			String dbField = uiFieldToDBField.get(field);
			if (null != dbField) {
				
				// Filter out all the placements fields that are not required for the item's mounting
				if (mountingPlacementFieldsMap.get("allMountingFields").contains(field)) {
				
					if (mountingPlacementFieldsMap.get(mounting).contains(field)) {
						queryMapDBFieldToAlias.put(dbField, field);
					}
				}
				// Collect all non-filtered fields
				else {
					queryMapDBFieldToAlias.put(dbField, field);
				}
			}
			else {
				// TODO:: we may need to process all the sets
			}
			
		}
		
		if (log.isDebugEnabled()) log.debug("\n filtered non Editable Fields Size = " + queryMapDBFieldToAlias.size() + "\nFiltered Non-Editable Fields: " + queryMapDBFieldToAlias);

		return queryMapDBFieldToAlias;
		
	}
	

	/**
	 * compare the values in DB against the values provided by the user for non-editable fields
	 * @param item
	 * @param dbValues
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private List<String> compareNonEditableFieldsValue(Item item, Map<String, Object> dbValues) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		// Compare the value in the item map against the value in the DB map
		List<String> fieldNotEqual = new ArrayList<String>();
		
		// for each of the non-editable fields
		for (Map.Entry<String, Object> entry: dbValues.entrySet()) {
			
			// get the uiId
			String uiField = entry.getKey();
			// get the value in DB
			Object dbValue = entry.getValue();
			// get user provided value
			// Object userValue = ValueIdDTOHolder.getCurrent().getValue(entry.getKey());
			Object userValue = getUserValue(item, uiViewToHibernateField.get(uiField));
			
			if (log.isDebugEnabled()) log.debug("field = " + entry.getKey() + ": db value = " + entry.getValue() + " | user value = " + userValue);
			
			// compare the value in DB against the value provided by the user
			Boolean equal = false;
			// If DB and user provided value is null, it is equal
			if (null == dbValue && null == userValue) {
				equal = true;
			}
			// If DB has a value and user provided null
			else if (null != dbValue && null == userValue) {
				// A blank in DB can be considered as null by the user
				equal = dbValue.equals("");
				// If DB is not blank, use the equals to compare the values
				if (!equal) {
					equal = dbValue.equals(userValue);
				}
			}
			// If DB has a null and user provided the value, then
			else if (null == dbValue && null != userValue) {
				// if DB value is 'null', check all allowable user values against the 'null' in DB
				if (validNullValueInDBMap.containsKey(uiField)) {
					equal = validNullValueInDBMap.get(uiField).contains(userValue);
				}
				// If the value is not in the allowable 'null' list, then check the values using equals
				if (!equal) { 
					equal = userValue.equals(dbValue);
				}
			}
			// If the value that are being compared in a Number, the use Number comparison.
			// We have to make sure that the type doesn't matter but the value
			else if (null != dbValue && null != userValue && dbValue instanceof Number) {
				int diff = numberCompare(dbValue, userValue);
				equal = (diff == 0);
			}
			// If not a Number, then use the object's equals API
			else {
				equal = userValue.equals(dbValue);
			}
			
			// if the non-editable field value is not same: collect the field in the list of edited fields
			if (!equal) {
				fieldNotEqual.add(uiField);
			}
		}
		
		return fieldNotEqual;

	}

	/**
	 * generate error message if any non-editable fields is/are modified
	 * @param fieldNotEqual
	 * @param errors
	 */
	private void generateErrorMessage(List<String> fieldNotEqual, Errors errors) {
		// Generate the error message for the user
		if (fieldNotEqual.size() > 0) {
			
			StringBuffer fieldsEdited = new StringBuffer();
			for (String editedField: fieldNotEqual) {
				fieldsEdited.append("\n\t");
				fieldsEdited.append(uiViewToUserReadableName.get(editedField));
			}
			fieldsEdited.append("\n");
			
			Object[] errorArgs = { fieldsEdited.toString(), (fieldNotEqual.size() > 1) ? "fields" :  "field", (fieldNotEqual.size() > 1) ? "are" :  "is" };
			errors.rejectValue("cmbStatus", "ItemValidator.fieldsNotEditable", errorArgs, "Cannot edit the following fields: " + fieldNotEqual.toString());
		}

	}
}
