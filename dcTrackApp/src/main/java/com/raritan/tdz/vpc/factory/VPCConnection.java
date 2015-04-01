package com.raritan.tdz.vpc.factory;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * interface to use to connect vpc items
 * @author bunty
 *
 */
public interface VPCConnection {

	/**
	 * create connections using the VPC items
	 * @param vpcItems
	 * @throws BusinessValidationException 
	 */
	public void create(Map<String, List<Item>> vpcItems) throws BusinessValidationException;
	
}
