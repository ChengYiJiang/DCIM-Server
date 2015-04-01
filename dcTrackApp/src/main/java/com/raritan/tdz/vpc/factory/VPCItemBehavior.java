package com.raritan.tdz.vpc.factory;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;

/**
 * creation and link behavior for individual VPC item class
 * @author bunty
 *
 */
public interface VPCItemBehavior {

	/**
	 * create the VPC Item
	 * @param locationId TODO
	 * @param vpcPath TODO
	 * @param vpcItems
	 * @param errors
	 */
	public void create(Long locationId, String vpcPath, Map<String, List<Item>> vpcItems, Errors errors);
	
	/**
	 * link the VPC items
	 * @param vpcItems
	 * @param errors
	 */
	public void link(Map<String, List<Item>> vpcItems, Errors errors);
	
}
