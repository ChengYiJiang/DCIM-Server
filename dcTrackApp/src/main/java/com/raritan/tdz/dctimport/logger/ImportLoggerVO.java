/**
 * 
 */
package com.raritan.tdz.dctimport.logger;

import java.util.HashMap;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

/**
 * @author prasanna
 * This is a Pojo that keeps the lines, errors and warnings 
 * which will be assembled/processed by the import logger 
 */
public class ImportLoggerVO {
	private String line;
	private int lineNumber;
	private final Errors errors = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName() + "Error");
	private final Errors warnings  = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName() + "Warning");
	


	public void setLine(String line) {
		this.line = line;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setErrors(Errors errors) {
		for (ObjectError error:errors.getAllErrors()){
			this.errors.reject(error.getCode(),error.getArguments(),error.getDefaultMessage());
		}
	}

	public void setWarnings(Errors warnings) {
		for (ObjectError warning:warnings.getAllErrors()){
			this.errors.reject(warning.getCode(),warning.getArguments(),warning.getDefaultMessage());
		}
	}

	public String getLine() {
		return line;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public Errors getErrors() {
		return errors;
	}

	public Errors getWarnings() {
		return warnings;
	}
}
