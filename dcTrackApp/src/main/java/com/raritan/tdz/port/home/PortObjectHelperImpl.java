package com.raritan.tdz.port.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.port.dao.PortDAO;

public class PortObjectHelperImpl<T extends Serializable> implements PortObjectHelper<T> {

	protected Class<T> type;

	/** The message source */
	protected ResourceBundleMessageSource messageSource;

	@Autowired(required=true)
	ValidateRequiredFieldLks validateRequiredFieldLks;
	
	@Autowired(required=true)
	ValidateRequiredFieldLku validateRequiredFieldLku;
	
	@Autowired(required=true)
	ValidateFieldLength validateFieldLength;

	public Class<T> getType() {
		return type;
	}

	public void setType(Class<T> type) {
		this.type = type;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public PortObjectHelperImpl(Class<T> type) {
		super();
		this.type = type;
	}

	@Override
	public void init(IPortInfo port, Set<Long> handledPortSubclass, Set<Long> handledItemClass, String errorCode, Errors errors) {
		long actualPortSubclass = (null != port && null != port.getPortSubClassLookup() && null != port.getPortSubClassLookup().getLkpValueCode()) ? 
				port.getPortSubClassLookup().getLkpValueCode().longValue() : null;
		if (null == handledPortSubclass || !handledPortSubclass.contains(actualPortSubclass)) {
			if (null == errors) {
				throw new InvalidPortObjectException("Invalid port subclass " + actualPortSubclass);
			}
			else {
				Object[] errorArgs = { port.getPortName(), (null != port.getPortSubClassLookup() && null != port.getPortSubClassLookup().getLkpValue()) ? port.getPortSubClassLookup().getLkpValue() : " "};
				errors.rejectValue("Ports", errorCode, errorArgs, "Port: '" + port.getPortName() + "' type: " + port.getPortSubClassLookup().getLkpValue() + "is not supported for this item");
			}
		}
		
		Long actualItemClass = null;
		if (null != port && null != port.getItem() && null != port.getItem().getClassLookup() && null != port.getItem().getClassLookup().getLkpValueCode()) { 
			actualItemClass = port.getItem().getClassLookup().getLkpValueCode();
		}
		if (null != handledItemClass && (handledItemClass.contains(-1L) || handledItemClass.contains(actualItemClass))) {
			// port is good
		}
		else {
			if (null == errors) {
				throw new InvalidPortObjectException("Invalid item class " + actualItemClass.toString());
			}
			else {
				Object[] errorArgs = { port.getPortName(), (null != port.getPortSubClassLookup() && null != port.getPortSubClassLookup().getLkpValue()) ? port.getPortSubClassLookup().getLkpValue() : " "};
				errors.rejectValue("Ports", errorCode, errorArgs, "Port: '" + port.getPortName() + "' type: " + port.getPortSubClassLookup().getLkpValue() + "is not supported for this item");
			}
		}
	}

	@Override
	public void delete(IPortInfo portInfo, Dao<T> dao) {
		if (portInfo.getPortId() <= 0) {
			return;
		}
		dao.delete(getPort(portInfo));
	}

	@Override
	public T refresh(IPortInfo portInfo, PortDAO<T> dao) {
		T port = getPort(portInfo);
		port = dao.getPort(portInfo.getItem().getItemId(), portInfo.getPortSubClassLookup().getLkpValueCode(), portInfo.getPortName());
		return port;
	}
	
	@Override
	public void save(IPortInfo portInfo, Dao<T> dao) {
		T port = getPort(portInfo);
		if (portInfo.getPortId() == null || portInfo.getPortId() <= 0) {
			dao.create(port);
		}
		else {
			dao.merge(port);
		}
	}

	private T getDetachedPortObject(IPortInfo portInfo, PortDAO<T> portDAO) {
		if (null != portInfo && null != portInfo.getPortId() && portInfo.getPortId().longValue() > 0) {
			return portDAO.loadEvictedPort(portInfo.getPortId());
		}
		return null;
	}

	private Object getValue(Object port, String methodName) {
		Object value = null;
		try {
			value = PropertyUtils.getProperty(port, methodName);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for " + methodName + ": Internal Error");
		}
		return value;
	}
	
	private Object getFieldValue(IPortInfo portInfo, String fieldName, PortDAO<T> portDAO) {
		
		if (null != portInfo && null != portInfo.getPortId() && portInfo.getPortId().longValue() > 0) {
			
			List<String> fields = new ArrayList<String>();
			fields.add(fieldName);
			Map<String, Object> fieldMap = portDAO.getFieldsValue(this.type, "portId", portInfo.getPortId().longValue(), fields); //loadEvictedPort(portInfo.getPortId());
			
			return (null != fieldMap) ? fieldMap.get(fieldName) : null;
		}

		return null;
		
	}

	private Map<String, Object> getFieldsValue(IPortInfo portInfo, List<String> fields, PortDAO<T> portDAO) {
		
		if (null != portInfo && null != portInfo.getPortId() && portInfo.getPortId().longValue() > 0) {
			fields.remove("portId");
			Map<String, Object> fieldMap = portDAO.getFieldsValue(this.type, "portId", portInfo.getPortId().longValue(), fields);

			return fieldMap;
		}

		return null;
		
	}
	
	private Map<String, Object> getFieldsValue(IPortInfo portInfo, List<String> fields) {
		
		if (null != portInfo && null != portInfo.getPortId() && portInfo.getPortId().longValue() > 0) {
			fields.remove("portId");
			
			Map<String, Object> fieldMap = new HashMap<String, Object>();
			
			for (String field: fields) {
				fieldMap.put(field, getValue(this.type.cast(portInfo), field));
			}
			
			return fieldMap;
		}

		return null;
		
	}

	
	@Override
	public void setValue(Object port, String fieldName, Object value) {
		try {
			PropertyUtils.setProperty(port, fieldName, value);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find setter for " + fieldName + ": Internal Error");
		}
	}
	
	// PortValidator.connectedDataPortCannotDelete or PortValidator.connectedPowerPortCannotDelete or PortValidator.connectedSensorPortCannotDelete
	@Override
	public void validateDelete(IPortInfo portInfo, PortDAO<T> portDAO, Errors errors, String errorCode, IPortInfo savedPort) {
		
		/*T port = getDetachedPortObject(portInfo, portDAO);
		if (null == port) {
			return;
		}
		
		Boolean used = (Boolean) getValue(port, "used");*/
		
		Boolean used = null;
		if (null != savedPort) {
			used = (Boolean) getValue((this.type.cast(savedPort)), "used");
		}
		else {
			used = (Boolean) getFieldValue(portInfo, "used", portDAO);
		}
		if (null != used && used) {

			Object errorArgs[]  = {portInfo.getPortName() };
			errors.rejectValue("ports", errorCode, errorArgs, "Port is in use, cannot delete");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getPort(IPortInfo portInfo) {
		
		T port = null;
		if (null != portInfo) {
			port = (T) portInfo;
		}
		else {
			throw new InvalidPortObjectException("Invalid port during save validation");
		}
		return port;
	}

	@Override
	public boolean isModified(IPortInfo portInfo, PortDAO<T> portDAO) {
		
		T port = getDetachedPortObject(portInfo, portDAO); // TODO:: implement the equal function locally and then check for the fields separately
		return (null != port) ? !port.equals(portInfo) : false;
	}

	// "PortValidator.connectedDataPortCannotEdit"
	@Override
	public void validateEdit(IPortInfo portInfo, Errors errors, PortDAO<T> portDAO, List<String> fields, String errorCode, IPortInfo savedPort) {
		if (portInfo.getPortId() == null || portInfo.getPortId() <= 0) {
			return;
		}
		if (errorExist(errors, errorCode, null)) {
	    	return;
	    }
		// T rhs = getDetachedPortObject(portInfo, portDAO);
		T lhs = getPort(portInfo);
		if (/*null == rhs || */ null == lhs) {
			return;
		}
		/*if (!((Boolean)getValue(rhs, "used"))) {
			return;
		}*/
		fields.remove("portId");
		List<String> allFields = new ArrayList<String>(fields);
		allFields.add("used");
		Map<String, Object> fieldMap = null;
		if (null != savedPort) {
			fieldMap = getFieldsValue(savedPort, allFields);
		}
		else {
			fieldMap = getFieldsValue(portInfo, allFields, portDAO);
		}
		
		if (null == fieldMap) {
			return;
		}
		
		Boolean used = (Boolean) fieldMap.get("used");  //getFieldValue(portInfo, "used", portDAO);
		if (null == used || !used) {
			return;
		}
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		// Map<String, Object> fieldMap = getFieldsValue(portInfo, fields, portDAO);
		for (String field: fields) {
			// equalsBuilder.append(getValue(lhs, field), getValue(rhs, field));
			equalsBuilder.append(getValue(lhs, field), fieldMap.get(field));
		}
		boolean editValid = equalsBuilder.isEquals();
		if (!editValid) {
			Object errorArgs[]  = { };
			errors.rejectValue("tabDataPorts", errorCode, errorArgs, "Port is in use, can only edit certain fields");
		}
	}

	private boolean errorExist(Errors errors, String errorCode, String errorMessage) {
		List<ObjectError> errorList = errors.getAllErrors();
		for (ObjectError error : errorList) {
			String msg = messageSource.getMessage(error, Locale.getDefault());
			if (error.getCode().equals(errorCode) && (null == errorMessage || msg.contains(errorMessage)) ) {
				return true;
			}
		}
		return false;
	}
	
	// "PortValidator.dataPortFieldRequired" , "PortValidator.powerPortFieldRequired", "PortValidator.sensorPortFieldRequired" 
	@Override
	public void validateRequiredFields(IPortInfo portInfo, Errors errors,
			PortDAO<T> portDAO, Map<String, String> fields, String errorCode) {
		
		T port = getPort(portInfo);
		
		String requiredFieldsNotProvided = new String();
		
		for (Map.Entry<String, String> entry : fields.entrySet()) {
		    String field = entry.getKey();
		    Object value = getValue(port, field);
		    String userFriendlyFieldName = entry.getValue();
		    if (errorExist(errors, errorCode, "'" + userFriendlyFieldName + "'")) {
		    	continue;
		    }
		    if (value == null) {
		    	requiredFieldsNotProvided += "'" + userFriendlyFieldName + "'";
		    } else if (value instanceof LkuData) {
		    	validateRequiredFieldLku.validate(port, errors, field, errorCode, userFriendlyFieldName);
		    }
		    else if (value instanceof LksData) {
		    	validateRequiredFieldLks.validate(port, errors, field, errorCode, userFriendlyFieldName);
		    }
		    else if (value instanceof String) {
		    	if (((String)value).length() == 0) {
		    		requiredFieldsNotProvided += "'" + userFriendlyFieldName + "'";
		    	}
		    }
		    else {
		    	// Cannot validate field
		    	throw new InvalidPortObjectException("Cannot validate required field " + field + ": Internal Error");
		    }
		}
		if (requiredFieldsNotProvided.length() > 0) {
			Object[] errorArgs = {requiredFieldsNotProvided};
			errors.rejectValue("Ports", errorCode, errorArgs, "Port required fields not provided");
		}

	}

	// "PortValidator.dataPortNameLength", "PortValidator.powerPortNameLength", "PortValidator.sensorPortNameLength"
	@Override
	public void validateFieldLength(IPortInfo portInfo, Errors errors,
			String field, String errorCode, Long minLength, Long maxLength) {
		T port = getPort(portInfo);
		Object value = getValue(port, field);
		if (null != value) {
			if (value instanceof String) {
				if (((String)value).length() > maxLength || ((String)value).length() < minLength) {
					Object[] errorArgs = { portInfo.getPortName() };
					errors.rejectValue("Ports", errorCode, errorArgs, "Port field: " + field + " has invalid length");
				}
			}
			else {
		    	// Cannot validate field
		    	throw new InvalidPortObjectException("Cannot validate length of field " + field + ": Internal Error");
		    }
		}
	}
	
	// PortValidator.dataPortFieldRequired, PortValidator.powerPortFieldRequired, PortValidator.sensorPortFieldRequired
	@Override
	public void validateIfTrue(IPortInfo port, Errors errors,
			String errorCode, Boolean valid, String userFriendlyField) {

		String requiredFieldsNotProvided = new String();
		if (errorExist(errors, errorCode, "'" + userFriendlyField + "'")) {
			return;
		}
		if (!valid) {
			requiredFieldsNotProvided += "'" + userFriendlyField + "'";
		}

		if (requiredFieldsNotProvided.length() > 0) {
			Object[] errorArgs = {requiredFieldsNotProvided};
			errors.rejectValue("Ports", errorCode, errorArgs, "Port required fields not provided");
		}

	}


	@Override
	public void applyCommonAttributes(IPortInfo portInfo, IPortInfo refPortInfo,
			Errors errors, Map<String, String> fields, String errorCode) {

		if (null == refPortInfo /*&& !(refPort instanceof getType().getClass())*/) { // TODO:: Use instanceof on Class<T> type
			return;
		}
		
		T port = getPort(portInfo);
		@SuppressWarnings("unchecked")
		T refPort = (T) refPortInfo;

		for (Map.Entry<String, String> entry : fields.entrySet()) {
		    
			String field = entry.getKey();
		    
		    Object refValue = getValue(refPort, field);
		    
		    // set value Object value 
		    setValue(port, field, refValue);
		    
		}
		
	}

	
	@Override
	public void validateCommonAttributes(IPortInfo portInfo, IPortInfo refPortInfo,
			Errors errors, Map<String, String> fields, String errorCode) {

		if (null == refPortInfo /*&& !(refPort instanceof getType().getClass())*/) { // TODO:: Use instanceof on Class<T> type
			return;
		}
		
		T port = getPort(portInfo);
		@SuppressWarnings("unchecked")
		T refPort = (T) refPortInfo;
		String nonCommonArrtibutes = new String();

		for (Map.Entry<String, String> entry : fields.entrySet()) {
		    String field = entry.getKey();
		    Object value = getValue(port, field);
		    Object refValue = getValue(refPort, field);
		    String userFriendlyFieldName = entry.getValue();
		    
		    if (errorExist(errors, errorCode, "'" + userFriendlyFieldName + "'")) {
		    	continue;
		    }
		    
		    if (value != null && refValue != null && !value.equals(refValue)) {
				nonCommonArrtibutes += "'" + userFriendlyFieldName + "'";
			}
		}

		if (nonCommonArrtibutes.length() > 0) {
			Object errorArgs[]  = { nonCommonArrtibutes };
			errors.rejectValue("Ports", errorCode, errorArgs, "Port common attributes are not same");
		}
		
	}

	// PortValidator.powerIncorrectFieldValue PortValidator.dataIncorrectFieldValue PortValidator.sensorIncorrectFieldValue
	@Override
	public void validateFieldValueRange(IPortInfo portInfo, Errors errors,
			String field, String userFriendlyFieldName, String errorCode, Long moreThanValue,
			Long maxValue) {
		
		if (errorExist(errors, errorCode, "'" + userFriendlyFieldName + "'")) {
			return;
		}
		
		T port = getPort(portInfo);
		
		String invalidValueFields = new String();
		
	    boolean minConditionFailed = false;
	    boolean maxConditionFailed = false;
		
	    Object value = getValue(port, field);
	    if (null != moreThanValue) {
	    	if (value instanceof Integer) {
	    		minConditionFailed = ((Integer)value <= moreThanValue); 
	    	} else if (value instanceof Long) {
	    		minConditionFailed = ((Long)value <= moreThanValue); 
	    	} else if (value instanceof Double) {
	    		minConditionFailed = ((Double)value <= moreThanValue); 
	    	} else if (value instanceof String) {
	    		minConditionFailed = (((String)value).length() <= moreThanValue);
	    	} else if (null == value) {
	    		if (moreThanValue > 0) {
	    			minConditionFailed = true;
	    		}
	    	}
	    	else {
	    		// Cannot validate field
		    	throw new InvalidPortObjectException("Cannot validate min range of field " + field + ": Internal Error");
	    	}
	    }
	    if (null != maxValue) {
	    	if (value instanceof Integer) {
	    		maxConditionFailed = ((Integer)value > maxValue); 
	    	} else if (value instanceof Long) {
	    		maxConditionFailed = ((Long)value > maxValue); 
	    	} else if (value instanceof Double) {
	    		maxConditionFailed = ((Double)value > maxValue); 
	    	} else if (value instanceof String) {
	    		maxConditionFailed = (((String)value).length() > maxValue);
	    	} else if (null == value) {
	    		// Nothing to validate against
	    	}
	    	else {
	    		// Cannot validate field
		    	throw new InvalidPortObjectException("Cannot validate max range of field " + field + ": Internal Error");
	    	}
	    }
	    
	    if ((minConditionFailed || maxConditionFailed)) {
			invalidValueFields += "'" + userFriendlyFieldName + "'";
		}
		
		if (invalidValueFields.length() > 0) {
			Object[] errorArgs = {invalidValueFields};
			errors.rejectValue("Ports", errorCode, errorArgs, "Port fields value incorrect");
		}
	}

	
}
		

