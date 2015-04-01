/**
 * 
 */
package com.raritan.tdz.piq.home;

import com.raritan.tdz.domain.Item;

/**
 * @author prasanna
 *
 */
public class PIQItem {

	private Item item;
	private String ipAddress;
	private Integer powerRating;
	
	public PIQItem(Item item, String ipAddress, Integer powerRating){
		this.item = item;
		this.ipAddress = ipAddress;
		this.powerRating = powerRating;
	}

	/**
	 * @return the item
	 */
	public final Item getItem() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public final void setItem(Item item) {
		this.item = item;
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
	 * @return the powerRating
	 */
	public final Integer getPowerRating() {
		return powerRating;
	}

	/**
	 * @param powerRating the powerRating to set
	 */
	public final void setPowerRating(Integer powerRating) {
		this.powerRating = powerRating;
	}
	
}
