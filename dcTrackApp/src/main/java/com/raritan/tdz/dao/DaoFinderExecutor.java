/**
 * 
 */
package com.raritan.tdz.dao;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author prasanna
 *
 */
public interface DaoFinderExecutor<T extends Serializable> {
	public List<T> executeFinder(Method method, final Object[] queryArgs, boolean readOnly);
	public List<T> executeFetch(Method method, final Object[] queryArgs);
}
