package com.raritan.tdz.vpc.factory;


import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

/**
 * link the power outlet and the power panel
 * @author bunty
 *
 */
public class VPCItemLinkPowerOutletAndPowerPanel implements VPCItemLink { 

	@Override
	public void link(Map<String,List<Item>> vpcItems) {

		List<Item> powerPanels = vpcItems.get(VPCItemFactory.FLOOR_PDU_PANEL);
		List<Item> powerOutlets = vpcItems.get(VPCItemFactory.POWER_OUTLET);
		
		if (powerPanels.size() != 1) return;
		
		for (Item panelItem: powerPanels) {
		
			MeItem powerPanel = (MeItem) panelItem;
			
			for (Item outletItem: powerOutlets) {
				
				MeItem powerOutlet = (MeItem) outletItem;
				
				powerOutlet.setPduPanelItem(powerPanel);
			}
			
		}

	}

}
