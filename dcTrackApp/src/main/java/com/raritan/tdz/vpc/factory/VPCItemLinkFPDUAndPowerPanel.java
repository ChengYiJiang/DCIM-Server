package com.raritan.tdz.vpc.factory;


import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

/**
 * link the floor pdu and the power panel
 * @author bunty
 *
 */
public class VPCItemLinkFPDUAndPowerPanel implements VPCItemLink {

	@Override
	public void link(Map<String,List<Item>> vpcItems) {

		List<Item> fpdus = vpcItems.get(VPCItemFactory.FLOOR_PDU);
		List<Item> powerPanels = vpcItems.get(VPCItemFactory.FLOOR_PDU_PANEL);
		
		if (fpdus.size() != 1) return;
		
		for (Item item: fpdus) {
			
			Integer nameTieBreaker = 1;
			
			MeItem fpdu = (MeItem) item;
		
			for (Item powerPanelItem: powerPanels) {
				
				MeItem powerPanel = (MeItem) powerPanelItem;
				
				// set parent item 
				powerPanel.setParentItem(fpdu);
		
				// set pdu panel item
				powerPanel.setPduPanelItem(fpdu);
				
				// set the name to be unique
				powerPanel.setItemName(powerPanel.getItemName() + "-" + nameTieBreaker.toString());
				nameTieBreaker++;
				
			}
			
		}
		
	}

}
