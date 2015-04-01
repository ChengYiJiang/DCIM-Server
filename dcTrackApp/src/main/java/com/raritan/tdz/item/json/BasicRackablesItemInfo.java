package com.raritan.tdz.item.json;

import com.raritan.tdz.domain.Item;

public class BasicRackablesItemInfo extends BasicItemInfo {
	
	public BasicRackablesItemInfo() {
	}
	
	@Override
	public void collectItemInfo( Item item ){
		super.collectItemInfo(item);
		setCmbUPosition(item.getuPosition());
	}
}
