/**
 * 
 */
package com.raritan.tdz.dctimport.integration.exceptions;

/**
 * @author prasanna
 * This is a specific exception when there is an incorrect header detected
 */
public class IncorrectHeaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String headerName;
	
	public IncorrectHeaderException(String headerName) {
		super();
		this.headerName = headerName;
	}

	public final String getHeaderName() {
		return headerName;
	}
}
