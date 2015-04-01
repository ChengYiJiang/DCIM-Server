package com.raritan.tdz.vpc.factory;


import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

/**
 * link ups bank and the floor pdu
 * @author bunty
 *
 */
public class VPCItemLinkUPSBankAndFPDU implements VPCItemLink {

	@Override
	public void link(Map<String,List<Item>> vpcItems) {

		List<Item> fpdus = vpcItems.get(VPCItemFactory.FLOOR_PDU);
		List<Item> upsBanks = vpcItems.get(VPCItemFactory.UPS_BANK);

		if (upsBanks.size() != 1) return;
		
		for (Item upsItem: upsBanks) {
			
			MeItem upsBank = (MeItem) upsItem;
			
			for (Item fpduItem: fpdus) {
				
				MeItem fpdu = (MeItem) fpduItem;
				
				// set the ups bank for the floor pdu
				fpdu.setUpsBankItem(upsBank);
				
				// set the phase same as the ups bank
				fpdu.setPhaseLookup(upsBank.getPhaseLookup());
				
				// set the line voltage same the ups bank rating voltage
				fpdu.setLineVolts(upsBank.getRatingV());
				
			}
			
		}
		
	}

}
