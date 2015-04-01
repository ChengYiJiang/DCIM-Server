package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.model.factory.GenericModelFactory;

/**
 * updates the floor pdu vpc items with the changes requested by the user
 * changes that is requested by the user is the voltage and that shall change
 * the data in the floor pdu item
 * @author bunty
 *
 */
public class FPDUVpcItemUpdate implements VPCItemUpdate {

	@Autowired
	private VPCItemUpdateHelper vpcItemUpdateHelper;
	
	@Autowired
	private GenericModelFactory floorPduGenericModelFactory;
	
	private Long itemClassLkp;
	
	public Long getItemClassLkp() {
		return itemClassLkp;
	}

	public void setItemClassLkp(Long itemClassLkp) {
		this.itemClassLkp = itemClassLkp;
	}

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		MeItem meItem = (MeItem) item;
		
		// update model
		meItem.setModel(floorPduGenericModelFactory.getModel());
		
		// update class
		vpcItemUpdateHelper.updateClass(item, itemClassLkp);
		
		// KVA is been updated via the factory bean, the factory is not working reliably
		meItem.setRatingKva(VPCLookup.DefaultValue.kVA);
		
		// set rating amps
		meItem.setRatingAmps(VPCLookup.DefaultValue.Current);
		
	}

}
