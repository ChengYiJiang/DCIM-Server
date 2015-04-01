package com.raritan.tdz.vpc.factory;


import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

/**
 * link the power outlet and the ups bank
 * @author bunty
 *
 */
public class VPCItemLinkPowerOutletAndUpsBank implements VPCItemLink {

	@Override
	public void link(Map<String,List<Item>> vpcItems) {

		List<Item> powerOutlets = vpcItems.get(VPCItemFactory.POWER_OUTLET);
		List<Item> upsBanks = vpcItems.get(VPCItemFactory.UPS_BANK);
		
		if (upsBanks.size() != 1) return;
		
		for (Item upsItem: upsBanks) {
		
			MeItem upsBank = (MeItem) upsItem;
			
			for (Item outletItem: powerOutlets) {
				
				MeItem powerOutlet = (MeItem) outletItem;
				
				powerOutlet.setUpsBankItem(upsBank);
			}
			
		}


	}

}
