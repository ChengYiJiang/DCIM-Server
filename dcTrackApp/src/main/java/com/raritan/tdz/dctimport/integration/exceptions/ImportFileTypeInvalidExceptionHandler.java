/**
 * 
 */
package com.raritan.tdz.dctimport.integration.exceptions;

import java.util.HashMap;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * @author prasanna
 *
 */
public class ImportFileTypeInvalidExceptionHandler implements
		ImportExceptionHandler {
	
	private Errors errors;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.exceptions.ImportExceptionHandler#handleException(java.lang.Throwable, java.lang.Object[])
	 */
	@Override
	public void handleException(Throwable cause, Object... ars) {
		errors = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName()); 
		errors.reject("Import.file.error");
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.exceptions.ImportExceptionHandler#getErrors()
	 */
	@Override
	public Errors getErrors() {
		return errors;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.exceptions.ImportExceptionHandler#getWarnings()
	 */
	@Override
	public Errors getWarnings() {
		// Return a null here
		return null;
	}

}
