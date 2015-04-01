package com.raritan.tdz.item.json;

import com.raritan.tdz.domain.Item;

public class BasicZeroUItemInfo extends BasicItemInfo {
	public BasicZeroUItemInfo() {
	}
	
	@Override
	public void collectItemInfo( Item item ){
		super.collectItemInfo(item);
		setCmbUPosition(item.getuPosition());
		if(item.getMountedRailLookup() != null ) {
			setRadioCabinetSide(item.getMountedRailLookup().getLkpValue());
		}
		if( item.getFacingLookup() != null ){
			setRadioDepthPosition(item.getFacingLookup().getLkpValue());
		}
	}
}
