package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.factory.GenericModelFactory;

/**
 * updates the UPS vpc items with the changes requested by the user
 * changes that is requested by the user is the voltage and that shall change
 * the data in the UPS item
 * @author bunty
 *
 */
public class UpsVpcItemUpdateStaticData implements VPCItemUpdate {

	@Autowired
	private LksCache lksCache;
	
	@Autowired
	private VPCItemUpdateHelper vpcItemUpdateHelper;

	@Autowired
	private GenericModelFactory upsGenericModelFactory;
	
	private Long itemClassLkp;
	
	public Long getItemClassLkp() {
		return itemClassLkp;
	}

	public void setItemClassLkp(Long itemClassLkp) {
		this.itemClassLkp = itemClassLkp;
	}

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		// update class
		vpcItemUpdateHelper.updateClass(item, itemClassLkp);
		
		// update generic model
		item.setModel(upsGenericModelFactory.getModel());
		
		MeItem meItem = (MeItem) item;
		
		Long voltageLkpValueCode = SystemLookup.VoltClass.V_480; 
		Long phaseLkpValueCode = SystemLookup.PhaseIdClass.THREE_DELTA;
		
		// update rating voltage and phase
		meItem.setRatingV(new Long(lksCache.getLksDataUsingLkpCode(voltageLkpValueCode).getLkpValue()).longValue());
		meItem.setPhaseLookup(lksCache.getLksDataUsingLkpCode(phaseLkpValueCode));
		
		// update kW
		meItem.setRatingKW(VPCLookup.DefaultValue.kW);
		
  		// update kVA
		meItem.setRatingKva(VPCLookup.DefaultValue.kVA);
		
	}

}
