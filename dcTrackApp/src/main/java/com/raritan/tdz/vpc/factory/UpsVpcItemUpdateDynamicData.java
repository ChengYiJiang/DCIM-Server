package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * update ups dynamic/configurable data
 * @author bunty
 *
 */
public class UpsVpcItemUpdateDynamicData implements VPCItemUpdate {

	@Autowired
	private LksCache lksCache;
	
	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		MeItem meItem = (MeItem) item;
		
		meItem.setRatingV(new Long(lksCache.getLksDataUsingLkpCode(SystemLookup.VoltClass.V_480).getLkpValue()).longValue());
		meItem.setPhaseLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PhaseIdClass.THREE_DELTA));
		
	}

}
