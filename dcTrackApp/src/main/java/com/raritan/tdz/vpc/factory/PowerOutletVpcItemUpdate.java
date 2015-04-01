package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.factory.GenericModelFactory;

/**
 * update power outlet
 * @author bunty
 *
 */
public class PowerOutletVpcItemUpdate implements VPCItemUpdate {

	@Autowired
	private VPCItemUpdateHelper vpcItemUpdateHelper;
	
	@Autowired
	private GenericModelFactory powerOutletGenericModelFactory;
	
	private Long itemClassLkp;
	
	private Long itemSubClassLkp;
	
	public Long getItemClassLkp() {
		return itemClassLkp;
	}

	public void setItemClassLkp(Long itemClassLkp) {
		this.itemClassLkp = itemClassLkp;
	}
	
	public Long getItemSubClassLkp() {
		return itemSubClassLkp;
	}

	public void setItemSubClassLkp(Long itemSubClassLkp) {
		this.itemSubClassLkp = itemSubClassLkp;
	}

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		// update model
		item.setModel(powerOutletGenericModelFactory.getModel());
		
		// update class
		vpcItemUpdateHelper.updateClass(item, itemClassLkp);
		
		// update subclass
		vpcItemUpdateHelper.updateSubClass(item, itemSubClassLkp);
		
		// set location reference for the power outlet to appear in the circuit list
		item.setLocationReference(VPCLookup.DefaultValue.poLocRef);
		
		// place the po to above, just providing a default value
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);
		
	}

}
