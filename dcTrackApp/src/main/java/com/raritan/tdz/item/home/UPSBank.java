package com.raritan.tdz.item.home;

import com.raritan.tdz.chassis.home.CompositeItem;
import com.raritan.tdz.exception.DataAccessException;

/**
 * Business layer representation of a UPS bank.
 * @author Andrew Cohen
 */
public interface UPSBank extends CompositeItem {
	
	/**
	 * Returns the number of UPS items linked to this UPS bank.
	 * @return
	 * @throws DataAccessException
	 */
	public int getLinkedUPSCount() throws DataAccessException;
}