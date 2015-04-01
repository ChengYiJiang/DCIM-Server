/**
 * 
 */
package com.raritan.tdz.dao;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.raritan.tdz.exception.DataAccessException;

/**
 * This is an executor that performs any update operations.
 * This will be implemented by the DaoImpl
 * @author prasanna
 *
 */
public interface DaoUpdateExecutor<T extends Serializable> {
	/**
	 * This will execute the named query for the update
	 * @param method
	 * @param queryArgs
	 * @return TODO
	 * @throws DataAccessException TODO
	 */
	public int executeUpdate(Method method, final Object[] queryArgs) throws DataAccessException;
}
