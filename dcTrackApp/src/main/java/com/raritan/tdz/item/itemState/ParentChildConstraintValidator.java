/**
 * 
 */
package com.raritan.tdz.item.itemState;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author prasanna
 * This takes care of validating parent child constraints for a given status of an item
 */
public interface ParentChildConstraintValidator {
	/**
	 * Validate parent-child constraint for a status that the given item will be after request is complete.
	 * @param item
	 * @param newStatusLkpValueCode The new status item is going to be.
	 * @param errors
	 * @param errorCodePrefix TODO
	 * @throws DataAccessException
	 * @throws ClassNotFoundException
	 */
	public void validateParentChildConstraint(Item item, Long newStatusLkpValueCode, Errors errors, String errorCodePrefix) throws DataAccessException;
}
