package com.raritan.tdz.dao;

/**
 * Throws this exception when an exception is thrown by the postgres
 * @author bunty
 *
 */
public class DAORuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1865627092738043985L;

	public DAORuntimeException(String message) {
		super(message);

	}
	
}
