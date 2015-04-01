package com.raritan.tdz.dctimport.integration.exceptions;

import java.util.HashMap;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * This error handler will ignore the exception, return no errors and no warnings 
 * This can be used anywhere where the errors are already been handled and import
 * processing has to be stopped without raising system exception
 * @author bunty
 *
 */
public class ImportIgnoreExceptionHandler implements ImportExceptionHandler {

	private final Errors errors = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName());
	
	
	@Override
	public void handleException(Throwable cause, Object... ars) {
		// DO NOT handle any exception
		
	}

	@Override
	public Errors getErrors() {

		return errors;
	}

	@Override
	public Errors getWarnings() {

		return errors;
	}

}
