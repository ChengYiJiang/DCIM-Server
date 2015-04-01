/**
 * 
 */
package com.raritan.tdz.dctimport.integration.exceptions;

import org.springframework.validation.Errors;


/**
 * @author prasanna
 * This is used by ImportErrorHandler implementation to handle specific exceptions
 * thrown during the import job execution.
 */
public interface ImportExceptionHandler {
	/**
	 * Translates exceptions to the corresponding error object
	 * Arguments are optional and can be null
	 * @param cause
	 * @param ars
	 */
	public void handleException(Throwable cause,Object... ars);
	
	/**
	 * On the final cuase get any errors
	 * @return - Can return null
	 */
	public Errors getErrors();
	
	/**
	 * On the final cause get any warnings
	 * @return - Can return null
	 */
	public Errors getWarnings();
}
