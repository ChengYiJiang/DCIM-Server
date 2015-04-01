package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.Item;

/**
 * Set the vpc item state to hidden
 * 
 * @author bunty
 *
 */
public class VPCItemUpdateState implements VPCItemUpdate {

	@Autowired
	private LksCache lksCache;
	
	private Long status;

	
	
	public VPCItemUpdateState(Long status) {

		this.status = status;
	}


	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		item.setStatusLookup(lksCache.getLksDataUsingLkpCode(status));

	}

}
