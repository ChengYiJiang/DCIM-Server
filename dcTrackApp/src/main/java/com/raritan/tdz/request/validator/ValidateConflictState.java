package com.raritan.tdz.request.validator;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

/**
 * 
 * @author bunty
 *
 */
public interface ValidateConflictState {

	/**
	 * Validate the conflicts between request and the item.
	 * @param item
	 * @param request
	 * @param requests
	 * @param errors
	 * @param itemName
	 * @param userInfo TODO
	 * @throws DataAccessException
	 */
	public void validate(Item item, Request request, List<Request> requests, Errors errors, String itemName, UserInfo userInfo) throws DataAccessException;
	
}
