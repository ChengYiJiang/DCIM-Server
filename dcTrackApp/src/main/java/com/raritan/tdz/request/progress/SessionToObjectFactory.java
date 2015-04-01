package com.raritan.tdz.request.progress;

import com.raritan.tdz.domain.UserInfo;

/**
 * The object factory will provide new object against the given session id 
 * @author bunty
 *
 */
public interface SessionToObjectFactory<T> {

	
	/**
	 * creates a new object if do not exist or clears the object if exist for a given user session 
	 * @param userInfo
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public T create(UserInfo userInfo) throws InstantiationException, IllegalAccessException;
	
	/**
	 * provide the object for a given session
	 * - creates a new object if do not exist in the map
	 * - else provides existing object 
	 * @param userInfo
	 * @return
	 */
	public T get(UserInfo userInfo);

	/**
	 * clears the dto from the map of session v/s userInfo
	 * @param userInfo
	 * @return TODO
	 */
	public T clear(UserInfo userInfo);

	/**
	 * clears the dto from the map of session v/s sessionId
	 * @param sessionId
	 * @return
	 */
	T clear(String sessionId);
	
}
