/**
 * 
 */
package com.raritan.tdz.vpc.factory;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;



/**
 * This creates VPC Items.
 * <p><b>Note that this does not save the items</b></p>
 * @author prasanna
 *
 */
public interface VPCItemFactory {
	
	/** 
	 * These static strings will be used as a key to the map returned
	 * by createVPCItems method and as the bean id that is created
	 * using ItemFactory
	 */
	public static final String POWER_OUTLET = "vpcPowerOutlet";
	public static final String FLOOR_PDU_PANEL = "vpcFloorPDUPanel";
	public static final String FLOOR_PDU = "vpcFloorPDU";
	public static final String UPS_BANK = "vpcUPSBank";
	public static final String UPS = "vpcUPS";
	
	
	/**
	 * This will create VPC Items.
	 * <p>The map returned will contain the type of item as key and the item.
	 * It is the responsibility of the caller to perform a save operation to 
	 * persist this into the database </p>
	 * @param locationId
	 * @return
	 */
	public Map<String, List<Item>> create(Long locationId);
}
