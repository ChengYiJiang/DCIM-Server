package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.UserInfo;


public interface IPortObjectCollection {
	
	/**
	 * deletes all the ports for RackPdu if the outlet voltage is invalid
	 * deletes the port if the connector is invalid
	 * @param itemObj
	 */
	public void deleteInvalidPorts(Errors errors);
	
	/**
	 * initializes the port objects with the target object data
	 * @param ports
	 */
	public void init(Object target, Errors errors);
	
	/**
	 * validates the port objects
	 * @param errors
	 */
	public void validate(Errors errors);

	/**
	 * delete the port objects
	 */
	public void delete();

	/**
	 * performs pre save operation
	 */
	public void preSave();

	/**
	 * save the port objects to the database
	 */
	public void save();
	
	/**
	 * performs post save operation
	 * @param userInfo 
	 * @param errors 
	 */
	public void postSave(UserInfo userInfo, Errors errors);

	/**
	 * Updates any data that is required to be updated before the validation is performed
	 */
	public void preValidateUpdates(Errors errors);
	
	/**
	 * clear the port move data associated with the port
	 * @param errors
	 */
	public void clearPortMoveData(Errors errors);

	/**
	 * apply the attributes common to all the ports for a given port subclass to all ports in the port objects
	 * @param refPort
	 * @param errors
	 */
	public void applyCommonAttributes(IPortObject refPort, Errors errors);
	

}
