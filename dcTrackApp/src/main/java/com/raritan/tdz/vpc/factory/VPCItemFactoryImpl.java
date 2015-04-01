/**
 * 
 */
package com.raritan.tdz.vpc.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;

/**
 * @author prasanna
 *
 */
public class VPCItemFactoryImpl implements VPCItemFactory {
	
	private String vpcPath;
	
	private List<VPCItemBehavior> vpcItemBehaviors;
	
	public VPCItemFactoryImpl(String vpcPath, List<VPCItemBehavior> vpcItemBehaviors) {

		this.vpcPath = vpcPath;
		
		this.vpcItemBehaviors = vpcItemBehaviors;
		
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.vpc.factory.VPCItemFactory#createVPCItems(java.lang.Long)
	 */
	@Override
	public Map<String, List<Item>> create(Long locationId) {

		Map<String, List<Item>> vpcItems = new HashMap<String, List<Item>>();
		
		for (VPCItemBehavior vpcBehavior: vpcItemBehaviors) {
			
			vpcBehavior.create(locationId, vpcPath, vpcItems, null);
			
		}
		
		for (VPCItemBehavior vpcBehavior: vpcItemBehaviors) {
			
			vpcBehavior.link(vpcItems, null);
			
		}
		
		return vpcItems;
	}

}
