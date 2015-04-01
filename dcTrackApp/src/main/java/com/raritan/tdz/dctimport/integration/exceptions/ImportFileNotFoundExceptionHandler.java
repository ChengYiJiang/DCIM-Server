/**
 * 
 */
package com.raritan.tdz.dctimport.integration.exceptions;

import java.util.HashMap;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * @author prasanna
 * FileNotFoundException handler for import files.
 */
public class ImportFileNotFoundExceptionHandler implements
		ImportExceptionHandler {
	
	private Errors errors;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.exceptions.ImportExceptionHandler#handleException(java.lang.Throwable, java.lang.Object[])
	 */
	@Override
	public void handleException(Throwable cause, Object... ars) {
		errors = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName()); 
		String fileName = "";
		if (cause.getMessage().contains("No such file or directory")){
			String msg = cause.getMessage();
			fileName = msg.substring(msg.lastIndexOf("/") + 1,msg.indexOf("(No such"));
		} else {
			String msg = cause.getMessage().substring(cause.getMessage().indexOf("[../../"));
			fileName = msg.substring(msg.lastIndexOf("/")+1,msg.indexOf("]"));
		}
		Object[] errorArgs = {fileName};
		errors.reject("Import.fileNotFound", errorArgs, "Import file not found");
	}

	@Override
	public Errors getErrors() {
		// TODO Auto-generated method stub
		return errors;
	}

	@Override
	public Errors getWarnings() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
