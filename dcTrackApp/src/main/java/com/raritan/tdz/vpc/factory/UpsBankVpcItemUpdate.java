package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.model.factory.GenericModelFactory;

/**
 * update ups bank data
 * @author bunty
 *
 */
public class UpsBankVpcItemUpdate implements VPCItemUpdate {

	@Autowired
	private VPCItemUpdateHelper vpcItemUpdateHelper;
	
	@Autowired
	private GenericModelFactory upsBankGenericModelFactory;
	
	private Long itemClassLkp;
	
	public Long getItemClassLkp() {
		return itemClassLkp;
	}

	public void setItemClassLkp(Long itemClassLkp) {
		this.itemClassLkp = itemClassLkp;
	}

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		MeItem upsBank = (MeItem) item;
		
		// update model
		item.setModel(upsBankGenericModelFactory.getModel());
		
		// update class
		vpcItemUpdateHelper.updateClass(item, itemClassLkp);
		
		// set redundancy
		upsBank.setPsredundancy(VPCLookup.DefaultValue.upsBankRedundancy);
		
	}

}
