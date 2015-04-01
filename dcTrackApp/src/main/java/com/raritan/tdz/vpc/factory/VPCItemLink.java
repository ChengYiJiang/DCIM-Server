package com.raritan.tdz.vpc.factory;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;


/**
 * link the vpc items with each other once the items are created
 * This linking is at the item level
 * @author bunty
 *
 */
public interface VPCItemLink {

	/**
	 * with the vpc items available link the items
	 * @param vpcItems - map of vpc constants item ref string to the items
	 */
	public void link(Map<String, List<Item>> vpcItems);
	
}
