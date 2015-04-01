package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

/**
 * update dynamic data of the power panel
 * @author bunty
 *
 */
public class PowerPanelVpcItemUpdateDynamicData implements VPCItemUpdate {

	@Autowired
	private LksCache lksCache;
	
	@Override
	public void update(Item item, Map<String, Object> additionalParams) {

		MeItem meItem = (MeItem) item;
		
		Long highVoltageLkpValueCode = (Long) additionalParams.get(VPCLookup.ParamsKey.HIGH_VOLTAGE_LKP);// vpcItemUpdateHelper.getHighVoltageLkp(locationId);
		Long lowVoltageLkpValueCode = (Long) additionalParams.get(VPCLookup.ParamsKey.LOW_VOLTAGE_LKP); // vpcItemUpdateHelper.getLowVoltageLkp(locationId);
		
		// set line voltage to high voltage
		meItem.setLineVolts(new Long(lksCache.getLksDataUsingLkpCode(highVoltageLkpValueCode).getLkpValue()).longValue());
		
		// set phase voltage to low voltage
		meItem.setPhaseVolts(new Long(lksCache.getLksDataUsingLkpCode(lowVoltageLkpValueCode).getLkpValue()).longValue());
		
	}

}
