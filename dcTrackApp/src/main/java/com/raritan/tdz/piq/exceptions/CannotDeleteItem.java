/**
 * 
 */
package com.raritan.tdz.piq.exceptions;

/**
 * @author prasanna
 *
 */
public class CannotDeleteItem extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3883679081559825746L;
	private Throwable cause;
	
	public CannotDeleteItem(Throwable cause){
		this.cause = cause;
	}

	public Throwable getCause() {
		return cause;
	}
}
