package com.raritan.tdz.request.progress;

import java.util.HashMap;
import java.util.Map;

import com.raritan.tdz.domain.UserInfo;

public class SessionToObjectFactoryImpl<T> implements SessionToObjectFactory<T> {

	protected Class<T> type;
	
	Map<String, T> sessionToFactoryObject;
	
	public SessionToObjectFactoryImpl(Class<T> type) {
		super();
		
		this.sessionToFactoryObject = new HashMap<String, T>();
		
		this.type = type;
		
	}

	@Override
	public T create(UserInfo userInfo) throws InstantiationException, IllegalAccessException {
		
		T factoryObject = get(userInfo);
		
		if (null == factoryObject) {
			factoryObject = type.newInstance();
			
			sessionToFactoryObject.put(userInfo.getSessionId(), factoryObject);
			
		}
		
		return factoryObject;
	}

	@Override
	public T get(UserInfo userInfo) {

		if (null == userInfo) return null;
		
		return sessionToFactoryObject.get(userInfo.getSessionId());
	}

	@Override
	public T clear(UserInfo userInfo) {

		return sessionToFactoryObject.remove(userInfo.getSessionId());
		
	}

	@Override
	public T clear(String sessionId) {

		return sessionToFactoryObject.remove(sessionId);
		
	}

}
