package com.raritan.tdz.request.validator;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * @author bunty
 *
 */

public interface RequestValidator {

	/**
	 * perform validations on the request when changing states
	 * @param requestMessage
	 * @throws ClassNotFoundException 
	 * @throws DataAccessException 
	 */
	public void validate(RequestMessage requestMessage) throws DataAccessException, ClassNotFoundException;
	
}
