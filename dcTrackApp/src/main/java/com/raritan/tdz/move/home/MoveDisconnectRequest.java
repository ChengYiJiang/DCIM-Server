package com.raritan.tdz.move.home;

import java.util.Map;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * interface to handle disconnect requests during item move
 * @author bunty
 *
 */
public interface MoveDisconnectRequest {

	/**
	 * issues disconnect request on the moving items
	 * @param itemIds
	 * @param requestMap
	 * @param user
	 * @throws BusinessValidationException
	 */
	public void disconnect(Map<Long, Long> itemIds, Map<Long,Long> requestMap, UserInfo user) throws BusinessValidationException;
	
}
