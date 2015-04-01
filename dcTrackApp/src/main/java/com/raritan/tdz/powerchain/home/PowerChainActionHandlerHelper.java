package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.powerchain.validator.ValidateObject;

/**
 * Helper for the power chain action handler
 * @author bunty
 *
 */
public interface PowerChainActionHandlerHelper {

	/**
	 * gets the new errors against the item
	 * @param item
	 * @return
	 * @throws BusinessValidationException 
	 */
	public MapBindingResult getErrorObject();
	
	/**
	 * validates if supports is true, else updates the errors with the errorCode
	 * @param supports
	 * @param errorCode
	 * @param errors
	 */
	public void validateSupportedItemClass(boolean supports, String errorCode, Errors errors);
	
	/**
	 * validates the supported port subclass
	 * @param supports
	 * @param errorCode
	 * @param errors
	 */
	public void validateSupportedPortSubClass(boolean supports, String errorCode, Errors errors);

	/**
	 * throws business validation exception if error exist
	 * @param errors
	 * @throws BusinessValidationException
	 */
	/*void throwBusinessValidationException(Errors errors)
			throws BusinessValidationException;*/

	/**
	 * validate if the item exist
	 * @param item
	 * @param errorCode
	 * @param errors
	 */
	void validateItem(Item item, String errorCode, Errors errors);
	
	/**
	 * find if the string is a numeric value
	 * @param str
	 * @return
	 */
	boolean isNumeric(String str);

	/**
	 * helps to create connection between source and dest port
	 * @param portConnection
	 * @param item
	 * @param srcPort
	 * @param destPort
	 * @param errors
	 * @param validateConnectionRatings
	 * @param migrationInProgress TODO
	 */
	void makeConnections(PortConnection portConnection, Item item,
			PowerPort srcPort, PowerPort destPort, Errors errors, boolean validateConnectionRatings, boolean migrationInProgress );

	/**
	 * create a breaker port and validates the values
	 * @param portFactory
	 * @param item
	 * @param portSubClass
	 * @param errors
	 * @return
	 */
	IPortInfo createPort(PortFactory portFactory, Item item, Long portSubClass,
			Errors errors);

	/**
	 * validate the ValidateObject that has the item, error code and information if it supports this kind of item
	 * @param validateObj
	 * @param errors
	 */
	void validateItem(ValidateObject validateObj, Errors errors);

	/**
	 * delete all connections from the source port
	 * @param portConnection
	 * @param item
	 * @param srcPort
	 * @param errors
	 * @param errorCode TODO
	 */
	void deleteConnections(PortConnection portConnection, Item item,
			PowerPort srcPort, Errors errors, String errorCode);

}
