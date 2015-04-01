package com.raritan.tdz.item.home.itemObject;

import org.springframework.validation.ObjectError;

/**
 * Restricts the max characters and max length size of error code 
 * @author bunty
 *
 */
public interface ErrorSizeController {

	public String getMessage(ObjectError error);
	
}
