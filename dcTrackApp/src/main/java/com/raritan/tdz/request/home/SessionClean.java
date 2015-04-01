package com.raritan.tdz.request.home;

import com.raritan.tdz.domain.UserInfo;

public interface SessionClean {

	/**
	 * clean the request context and its associated maps when the context is no more in use
	 * @param userInfo
	 */
	public void clean(UserInfo userInfo);
	
}
