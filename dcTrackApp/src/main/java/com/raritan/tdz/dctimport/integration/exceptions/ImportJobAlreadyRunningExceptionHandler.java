/**
 * 
 */
package com.raritan.tdz.dctimport.integration.exceptions;

import java.util.HashMap;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * @author prasanna
 * JobAlreadyRunningException handler for import files.
 */
public class ImportJobAlreadyRunningExceptionHandler implements
		ImportExceptionHandler {
	
	private Errors errors;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.exceptions.ImportExceptionHandler#handleException(java.lang.Throwable, java.lang.Object[])
	 */
	@Override
	public void handleException(Throwable cause, Object... ars) {
		errors =  new MapBindingResult(new HashMap<String, String>(), this.getClass().getName());
		Object[] errorArgs = {cause.getMessage()};
		errors.reject("Import.jobAlreadyRunning", errorArgs, "Job already running");
	}

	@Override
	public Errors getErrors() {
		return errors;
	}

	@Override
	public Errors getWarnings() {
		return null;
	}


}
