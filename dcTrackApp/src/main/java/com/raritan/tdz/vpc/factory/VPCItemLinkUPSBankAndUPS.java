package com.raritan.tdz.vpc.factory;


import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

/**
 * link the ups bank and the ups
 * @author bunty
 *
 */
public class VPCItemLinkUPSBankAndUPS implements VPCItemLink {

	@Override
	public void link(Map<String,List<Item>> vpcItems) {
		
		List<Item> upss = vpcItems.get(VPCItemFactory.UPS);
		List<Item> upsBanks = vpcItems.get(VPCItemFactory.UPS_BANK);
		
		if (upsBanks.size() != 1 || upss.size() != 1) return; 
		
		for (Item upsBankItem: upsBanks) {

			MeItem upsBank = (MeItem) upsBankItem;
			
			for (Item upsItem: upss) {
				
				MeItem ups = (MeItem) upsItem;
				
				// set the ups bank for the ups
				ups.setUpsBankItem(upsBank);
			}
		
		}

		for (Item upsItem: upss) {
			
			MeItem ups = (MeItem) upsItem;
			
			for (Item upsBankItem: upsBanks) {

				MeItem upsBank = (MeItem) upsBankItem;
			
				// set the phase of ups bank same as the ups
				upsBank.setPhaseLookup(ups.getPhaseLookup());
				
				// set the kVA same as ups
				upsBank.setRatingKva(ups.getRatingKva());
				
				// set the kW same as ups
				upsBank.setRatingKW(ups.getRatingKW());
				
				// set the rating voltage same as ups
				upsBank.setRatingV(ups.getRatingV());
			}
		}

	}

}
