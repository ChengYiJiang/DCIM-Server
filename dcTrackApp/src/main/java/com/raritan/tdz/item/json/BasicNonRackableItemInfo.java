package com.raritan.tdz.item.json;

import com.raritan.tdz.domain.Item;

public class BasicNonRackableItemInfo extends BasicItemInfo {
	public BasicNonRackableItemInfo(){
	}
	
	@Override
	public void collectItemInfo( Item item ) {
		super.collectItemInfo(item);
		setCmbUPosition(item.getuPosition());
		setCmbOrder(item.getShelfPosition());	
	}
}
