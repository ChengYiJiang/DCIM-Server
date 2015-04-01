package com.raritan.tdz.move.home;

import java.lang.reflect.InvocationTargetException;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * 
 * @author bunty
 *
 * @param <T>
 */
public interface PortMoveBehaviorHelper<T> {

	/**
	 * performs the post save operation for the moving item's port 
	 * @param item
	 * @param sessionUser
	 * @param additionalArgs
	 * @throws BusinessValidationException
	 * @throws DataAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 */
	void postSave(Item item, UserInfo sessionUser, Object[] additionalArgs)
			throws BusinessValidationException, DataAccessException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException;

}
