package com.raritan.tdz.vpc.factory;


import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

/**
 * link the ups bank and the power panel
 * @author bunty
 *
 */
public class VPCItemLinkUPSBankAndPowerPanel implements VPCItemLink {

	@Override
	public void link(Map<String,List<Item>> vpcItems) {

		List<Item> powerPanels = vpcItems.get(VPCItemFactory.FLOOR_PDU_PANEL);
		List<Item> upsBanks = vpcItems.get(VPCItemFactory.UPS_BANK);
		
		if (upsBanks.size() != 1) return;
		
		for (Item upsItem: upsBanks) {
		
			MeItem upsBank = (MeItem) upsItem;
			
			for (Item panelItem: powerPanels) {
				
				MeItem powerPanel = (MeItem) panelItem;
				
				powerPanel.setUpsBankItem(upsBank);
				
			}
		
		}

	}

}
