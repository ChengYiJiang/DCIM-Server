/**
 * 
 */
package com.raritan.tdz.item.itemState;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author prasanna
 * This is a mandatory field state validator interface
 * used to make specific code. There may be a base which 
 * is used by most of the states. For specific, we need
 * special code.
 */
public interface MandatoryFieldStateValidator {
	/**
	 * Validate mandatory fields
	 * @param item
	 * @param newStatusLkpValueCode The new status item is going to be.
	 * @param errors
	 * @param errorCodePostFix TODO
	 * @param request TODO
	 * @throws DataAccessException
	 * @throws ClassNotFoundException
	 */
	public void validateMandatoryFields(Item item, Long newStatusLkpValueCode, Errors errors, String errorCodePostFix, Request request) throws DataAccessException, ClassNotFoundException;
}
