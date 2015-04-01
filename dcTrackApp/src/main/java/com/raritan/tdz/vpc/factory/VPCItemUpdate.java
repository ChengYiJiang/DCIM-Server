package com.raritan.tdz.vpc.factory;

import java.util.Map;

import com.raritan.tdz.domain.Item;

/**
 * interface to use to update the columns of VPC item
 * @author bunty
 *
 */
public interface VPCItemUpdate {

	/**
	 * set the static values
	 * @param item
	 * @param additionalArgs TODO
	 */
	public void update(Item item, Map<String, Object> additionalParams);
	
}
