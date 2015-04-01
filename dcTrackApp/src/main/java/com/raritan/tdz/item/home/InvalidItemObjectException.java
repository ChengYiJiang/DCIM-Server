package com.raritan.tdz.item.home;

/**
 * An exception thrown when an ItemObject could not be obtained from an item domain object.
 * @author Andrew Cohen
 */
public class InvalidItemObjectException extends RuntimeException {

	public InvalidItemObjectException(String message) {
		super(message);
	}
}
