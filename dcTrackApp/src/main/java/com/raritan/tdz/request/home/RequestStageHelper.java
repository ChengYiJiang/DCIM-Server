package com.raritan.tdz.request.home;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;


/**
 * 
 * @author bunty
 *
 */

public interface RequestStageHelper {

	public void update(Request request, Long requestStage, UserInfo userInfo) throws Throwable;
	
}
