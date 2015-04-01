package com.raritan.tdz.vpc.factory;

import java.util.Map;

import com.raritan.tdz.domain.Item;

public class VpcItemUpdateNamePostfix implements VPCItemUpdate {

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {

		// set item name postfix 
		String namePostFix = (String) additionalParams.get(VPCLookup.ParamsKey.NAME_POSTFIX);
		item.setItemName(item.getItemName() + namePostFix);

	}

}
