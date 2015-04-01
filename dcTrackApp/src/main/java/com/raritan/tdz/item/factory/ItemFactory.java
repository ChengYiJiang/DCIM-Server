/**
 * 
 */
package com.raritan.tdz.item.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * This factory creates items given the uiValueIdDTO List as a map
 * @author prasanna
 *
 */
public interface ItemFactory {
	/**
	 * 
	 * @param propertyMap
	 * @param unit TODO
	 * @return
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public Item createItem(Map<String,Object> propertyMap, String unit) 
			throws IllegalAccessException, IllegalArgumentException, 
			InvocationTargetException, ClassNotFoundException, 
			BusinessValidationException, DataAccessException;
}
