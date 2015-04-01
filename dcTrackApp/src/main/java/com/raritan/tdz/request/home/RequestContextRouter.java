package com.raritan.tdz.request.home;

import org.springframework.integration.MessageChannel;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

public interface RequestContextRouter {

	public MessageChannel getContextChannel(UserInfo userInfo) throws DataAccessException;
	
	public void deleteContext(UserInfo userInfo);
	
}
