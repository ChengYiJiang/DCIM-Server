package com.raritan.tdz.item.json;

import com.raritan.tdz.domain.Item;

public class BasicNonRackablePowerOutletItemInfo extends BasicItemInfo {

	public BasicNonRackablePowerOutletItemInfo() {
	}
	
	@Override
	public void collectItemInfo( Item item ){
		super.collectItemInfo(item);
		setCmbOrder(item.getShelfPosition());
		setCmbUPosition(item.getuPosition());
	}
}
