package com.raritan.tdz.port.home;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.port.dao.PortDAO;

public interface PortObjectHelper<T extends Serializable> /*extends ValidateRequiredFieldLks, ValidateRequiredFieldLku, ValidateFieldLength*/  {

	/**
	 * initialize the port object and throws exception if the port do not support the given port subclass and item class
	 * @param port
	 * @param handledPortSubclass
	 * @param handledItemClass
	 */
	public void init(IPortInfo port, Set<Long> handledPortSubclass, Set<Long> handledItemClass, String errorCode, Errors errors);
	
	/**
	 * delete the port object
	 * @param dao
	 */
	public void delete(IPortInfo portInfo, Dao<T> dao);

	/**
	 * save the port object
	 * @param dao
	 */
	public void save(IPortInfo portInfo, Dao<T> dao);
	
	/**
	 * validates the delete operation against the used paramater
	 * @param savedPort TODO
	 * @param port
	 */
	public void validateDelete(IPortInfo portInfo, PortDAO<T> portDAO, Errors errors, String errorCode, IPortInfo savedPort);
	
	/**
	 * get the port object from the port info interface object
	 * @param port
	 * @return
	 */
	public T getPort(IPortInfo port);
	
	/**
	 * informs if the port is modified
	 * @param port
	 * @return
	 */
	public boolean isModified(IPortInfo portInfo, PortDAO<T> portDAO);
	
	/**
	 * validates if the list of fields are edited and updates the errors fields of changed
	 * @param port
	 * @param errors
	 * @param fields
	 * @param savedPort 
	 */
	public void validateEdit(IPortInfo port, Errors errors, PortDAO<T> portDAO, List<String> fields, String errorCode, IPortInfo savedPort);
	
	/**
	 * validate if the required fields are provided 
	 * @param port
	 * @param errors
	 * @param portDAO
	 * @param fields - map of the field and the corresponding user friendly name
	 * @param errorCode
	 */
	public void validateRequiredFields(IPortInfo port, Errors errors, PortDAO<T> portDAO, Map<String, String> fields, String errorCode);
	
	/**
	 * Validates the length of the field
	 * @param port
	 * @param errors
	 * @param fields
	 */
	public void validateFieldLength(IPortInfo port, Errors errors, String field, String errorCode, Long minLength, Long maxLength);
	
	/**
	 * Update the error if the validateAgainst is false
	 * @param port
	 * @param errors
	 * @param errorCode
	 * @param validateAgainst
	 * @param userFriendlyField
	 */
	public void validateIfTrue(IPortInfo port, Errors errors, String errorCode, Boolean validateAgainst, String userFriendlyField);
	
	/**
	 * Validates the common attributes against the reference port
	 * @param port
	 * @param refPort
	 * @param errors
	 * @param fields
	 * @param errorCode
	 */
	public void validateCommonAttributes(IPortInfo port, IPortInfo refPort, Errors errors, Map<String, String> fields, String errorCode);
	
	/**
	 * Applies the attribute common to all the port
	 * @param port
	 * @param refPort
	 * @param errors
	 * @param fields
	 * @param errorCode
	 */
	public void applyCommonAttributes(IPortInfo port, IPortInfo refPort, Errors errors, Map<String, String> fields, String errorCode);
	
	/**
	 * Validates if the value of the field in within the range
	 * @param port
	 * @param errors
	 * @param fields
	 * @param errorCode
	 * @param minValue
	 * @param maxValue
	 */
	public void validateFieldValueRange(IPortInfo port, Errors errors, String field, String userFriendlyFieldName, String errorCode, Long moreThanValue, Long maxValue);
	
	/**
	 * set the value to a given port
	 * @param port
	 * @param fieldName
	 * @param value
	 */
	public void setValue(Object port, String fieldName, Object value);

	/**
	 * merge the data in the session with the object
	 * @param portInfo
	 * @param dao
	 * @return T
	 */
	public T refresh(IPortInfo portInfo, PortDAO<T> dao);
	
}
