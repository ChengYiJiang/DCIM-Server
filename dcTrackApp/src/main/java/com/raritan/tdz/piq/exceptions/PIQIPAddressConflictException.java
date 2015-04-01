/**
 * 
 */
package com.raritan.tdz.piq.exceptions;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.piq.json.ErrorJSON;

/**
 * This exception is thrown when a device or PDU cannot be added to PIQ
 * because of an IP Addresss conflict.
 * 
 * @author prasanna
 */
public class PIQIPAddressConflictException extends PIQUpdateException {
	
	private static final long serialVersionUID = 1L;

	public PIQIPAddressConflictException(ErrorJSON piqError, Item item) {
		super( piqError, item );
	}
}
