package com.raritan.tdz.piq.exceptions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.ErrorJSON;

/**
 * The base exception class for all possible error conditions that may occur during a PIQ update.
 * The PIQ Update job will specifically listen for these exceptions and update the "skip count"
 * accordingly.
 * 
 * @author Andrew Cohen
 */
public abstract class PIQUpdateException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private List<String> piqMessages;
	private String piqErrorCode;
	
	private String ipAddress;
	private Integer proxyIndex;
	
	private Item item;
	
	public PIQUpdateException(ErrorJSON piqError, Item item) {
		if (piqError != null) {
			this.piqErrorCode = piqError.getError();
			this.piqMessages = piqError.getMessages();
		}
		else {
			this.piqErrorCode = "";
			this.piqMessages = new LinkedList<String>();
		}
		this.item = item;
	}
	
	/**
	 * @return the piqMessage
	 */
	public final List<String> getPiqMessages() {
		return piqMessages != null ? Collections.unmodifiableList( piqMessages ) : new LinkedList<String>();
	}

	/**
	 * @param piqMessage the piqMessage to set
	 */
	public final void setPiqMessages(List<String> piqMessages) {
		this.piqMessages = piqMessages;
	}

	/**
	 * @return the piqErrorCode
	 */
	public final String getPiqErrorCode() {
		return piqErrorCode;
	}

	/**
	 * @param piqErrorCode the piqErrorCode to set
	 */
	public final void setPiqErrorCode(String piqErrorCode) {
		this.piqErrorCode = piqErrorCode;
	}

	/**
	 * @return the ipAddress
	 */
	public final String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public final void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	/**
	 * Get the proxy index associated with the IP Address.
	 * @return
	 */
	public String getProxyIndex() {
		return proxyIndex != null ? proxyIndex.toString() : " ";
	}

	/**
	 * Set the proxy index associated with the IP address.
	 * @param proxyIndex
	 */
	public void setProxyIndex(Integer proxyIndex) {
		this.proxyIndex = proxyIndex;
	}

	/**
	 * @return the item
	 */
	public final Item getItem() {
		return item;
	}
	
	public final boolean isPdu() {
		if (item == null) return false;
		return item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU;
	}
}
