package com.raritan.tdz.vbjavabridge.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

public interface LNEventDAO extends Dao<LNEvent> {

	/**
	 * Set new insert, delete or update LN event. 
	 * @param lksId
	 * @param itemId
	 * @param CustomField3
	 * @return Number of record updated.
	 */
	int setLnEvent(Long lksId, Long itemId, String customField1,
			String customField2, String customField3);
	

}
