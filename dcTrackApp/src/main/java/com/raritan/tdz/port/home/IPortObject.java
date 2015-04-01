package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.UserInfo;
// import org.springframework.validation.Validator;

public interface IPortObject {

	/**
	 * set the port to the object
	 * @param port
	 */
	public void init(IPortInfo port, Errors errors);
	
	/**
	 * validate the power port save (new or edit)
	 * @param target - item object
	 * @param errors
	 */
	public void validateSave(Object target, Errors errors);
	
	/**
	 * validate the power port delete
	 * @param target - item object
	 * @param errors
	 */
	public void validateDelete(Object target, Errors errors);

	/**
	 * delete the port from the port table
	 */
	public void delete();
	
	/**
	 * 
	 * @return
	 */
	public boolean isConnectorValid();

	/**
	 * 
	 * @return
	 */
	public IPortInfo getPortInfo();

	/**
	 * 
	 * @return
	 */
	public boolean isModified();
	
	/**
	 * 
	 * @param errors
	 */
	public void validateCommonAttributes(IPortInfo refPort, Errors errors);
	
	/**
	 * save the port object to the database
	 */
	public void save();

	/**
	 * save the port object to the database
	 * @param userInfo 
	 * @param errors 
	 */
	public void postSave(UserInfo userInfo, Errors errors);

	/**
	 * save the port object to the database
	 * @return port
	 */
	public IPortInfo refresh();

	/**
	 * Updates any data that is required to be updated before the validation is performed
	 */
	public void preValidateUpdates(Errors errors);
	
	/**
	 * apply common attributes to the port using the reference port
	 * @param refPort
	 * @param errors
	 */
	public void applyCommonAttributes(IPortInfo refPort, Errors errors);
	
	/**
	 * set the value. Setter should be available for the given field
	 * @param port
	 * @param methodName
	 * @param value
	 */
	public void setValue(String fieldName, Object value);
	
	/**
	 * validate if the class/subclass of the item support the port
	 * @param target
	 * @param errors
	 */
	public void validateItemClassSubclass(Object target, Errors errors);

	
}
