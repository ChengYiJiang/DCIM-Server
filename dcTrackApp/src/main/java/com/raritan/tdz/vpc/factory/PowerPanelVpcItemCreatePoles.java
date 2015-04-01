package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.powerchain.home.PowerChainLookup.PowerPanelPoleNumbering;
import com.raritan.tdz.powerchain.home.PowerPanelPole;

/**
 * creates power panel's poles using poleQty 
 * @author bunty
 *
 */
public class PowerPanelVpcItemCreatePoles implements VPCItemUpdate {

	@Autowired
	private PowerPanelPole powerPanelPole;
	
	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		powerPanelPole.delete(item);
		
		powerPanelPole.create(item, PowerPanelPoleNumbering.SEQUENTIAL, 1L);

	}

}
