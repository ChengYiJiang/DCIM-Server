package com.raritan.tdz.piq.exceptions;

import com.raritan.tdz.domain.Item;


/**
 * This exception is thrown when attempting to add to PIQ an "installed" PDU in dcTrack
 * that does not have an IP Address.
 *  
 * @author Andrew Cohen
 */
public class InstalledPDUWithoutIPException extends PIQUpdateException {

	private static final long serialVersionUID = 1L;

	public InstalledPDUWithoutIPException(Item pduItem) {
		super( null, pduItem ); // No ErrorJSON because we detect this condition BEFORE sending to PIQ.
		setIpAddress( "" );
	}
}
