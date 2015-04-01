package com.raritan.tdz.cache;

import com.raritan.tdz.domain.LksData;

public interface LksCache {

	public LksData getLksDataUsingLkpCode(Long lkpValueCode);
	
	public LksData getLksDataUsingLkpAndType(String lkpValue, String type);
	
}
