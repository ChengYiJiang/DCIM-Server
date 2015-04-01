package com.raritan.tdz.item.json;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;


public class BasicCabinetItemInfo extends BasicItemInfo {

	public BasicCabinetItemInfo() {
	}
	
	@Override
	public void collectItemInfo( Item item ) {
		super.collectItemInfo(item);
			
		CabinetItem cabinet = (CabinetItem)item;
		setCmbRowLabel(cabinet.getRowLabel());
		setCmbRowPosition(cabinet.getPositionInRow());
	}

}
