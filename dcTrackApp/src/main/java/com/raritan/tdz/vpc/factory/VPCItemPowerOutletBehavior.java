package com.raritan.tdz.vpc.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

public class VPCItemPowerOutletBehavior implements VPCItemBehavior {

	@Autowired
	private ItemDAO itemDAO;
	
	private String vpcItemReference;
	
	private List<VPCItemUpdate> updaters;
	
	private List<VPCItemLink> linkers;
	
	@Autowired
	private VPCItemUpdateHelper vpcItemUpdateHelper;
	
	public VPCItemPowerOutletBehavior(String vpcItemReference,
			List<VPCItemUpdate> updaters, List<VPCItemLink> linkers) {

		this.vpcItemReference = vpcItemReference;
		this.updaters = updaters;
		this.linkers = linkers;
	}

	public List<VPCItemUpdate> getUpdaters() {
		return updaters;
	}

	public void setUpdaters(List<VPCItemUpdate> updaters) {
		this.updaters = updaters;
	}

	public String getVpcItemReference() {
		return vpcItemReference;
	}

	public void setVpcItemReference(String vpcItemReference) {
		this.vpcItemReference = vpcItemReference;
	}

	public List<VPCItemLink> getLinkers() {
		return linkers;
	}

	public void setLinkers(List<VPCItemLink> linkers) {
		this.linkers = linkers;
	}


	@Override
	public void create(Long locationId, String vpcPath,
			Map<String, List<Item>> vpcItems, Errors errors) {
		Map<String, Object> additionalParams = new HashMap<String, Object>();
		additionalParams.put(VPCLookup.ParamsKey.PATH, vpcPath);
		additionalParams.put(VPCLookup.ParamsKey.LOCATIONID, locationId);
		
		// Create power outlets based on the panel breakers
		List<Item> powerPanels = vpcItems.get(VPCItemFactory.FLOOR_PDU_PANEL);
		
		List<Item> items = new ArrayList<Item>();
		
		Integer nameTieBreaker = 1;
		for (Item powerPanel: powerPanels) {
			Set<PowerPort> ports = powerPanel.getPowerPorts();
			for (PowerPort port: ports) {
	
				// Create power outlet for only breaker ports
				if (!port.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) ||
						null == port.getPhaseLookup()) continue;
				
				// get the item from the factory
				Item item = vpcItemUpdateHelper.getNewItem(vpcItemReference);
				
				// update the item with all the updaters
				for (VPCItemUpdate update: updaters) { 
					additionalParams.put(PowerPort.class.getName(), port);
					additionalParams.put(VPCLookup.ParamsKey.NAME_POSTFIX, "-" + nameTieBreaker);
					update.update(item, additionalParams);
				}
				
				nameTieBreaker++;
				
				vpcItemUpdateHelper.addItem(item, items);
				
			}
		}
		vpcItems.put(vpcItemReference, items);

	}

	@Override
	public void link(Map<String, List<Item>> vpcItems, Errors errors) {
		
		if (null == linkers || 0 == linkers.size()) return;
		
		// run all linkers
		for (VPCItemLink linker: linkers) {
			
			linker.link(vpcItems);
			
		}
		
		List<Item> items = vpcItems.get(vpcItemReference);
		for (Item item: items) {
			itemDAO.saveItem(item);
		}

	}

}
