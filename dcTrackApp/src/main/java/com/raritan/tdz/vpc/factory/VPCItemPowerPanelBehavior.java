package com.raritan.tdz.vpc.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;

public class VPCItemPowerPanelBehavior implements VPCItemBehavior {

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private VPCItemUpdateHelper vpcItemUpdateHelper;
	
	private String vpcItemReference;
	
	private List<VPCItemUpdate> updaters;
	
	private List<VPCItemLink> linkers;
	
	// maps of low to high voltages for the power panels
	private Map<Long, Long> lowToHighVoltages;
	
	public VPCItemPowerPanelBehavior(String vpcItemReference,
			List<VPCItemUpdate> updaters, List<VPCItemLink> linkers, 
			Map<Long, Long> lowToHighVoltages) {

		this.vpcItemReference = vpcItemReference;
		this.updaters = updaters;
		this.linkers = linkers;
		this.lowToHighVoltages = lowToHighVoltages;
		
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
		
		List<Item> items = new ArrayList<Item>();
		
		// Create power panels for all voltages
		for (Map.Entry<Long, Long> voltage: lowToHighVoltages.entrySet()) {
			Long lowVoltageLkpValueCode = voltage.getKey();
			Long highVoltageLkpValueCode = voltage.getValue();
			
			// get the item from the factory
			Item item = vpcItemUpdateHelper.getNewItem(vpcItemReference);
			
			// update the item with all the updaters
			for (VPCItemUpdate update: updaters) { 

				additionalParams.put(VPCLookup.ParamsKey.LOW_VOLTAGE_LKP, lowVoltageLkpValueCode);
				additionalParams.put(VPCLookup.ParamsKey.HIGH_VOLTAGE_LKP, highVoltageLkpValueCode);
				update.update(item, additionalParams);
			}
			
			vpcItemUpdateHelper.addItem(item, items);
			
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
