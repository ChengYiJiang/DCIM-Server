package com.raritan.tdz.vpc.factory;

import java.util.Map;

import com.raritan.tdz.domain.Item;

/**
 * update the name of the power panel
 * @author bunty
 *
 */
public class PowerPanelVpcItemUpdateName implements VPCItemUpdate {

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		// set item name
		String path = (String) additionalParams.get(VPCLookup.ParamsKey.PATH);
		String name = "VPC-" + "Power Panel" + "-" + path;
		item.setItemName(name);


	}

}
