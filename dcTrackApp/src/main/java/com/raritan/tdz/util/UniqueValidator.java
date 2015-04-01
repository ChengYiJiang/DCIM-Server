/**
 * 
 */
package com.raritan.tdz.util;

import com.raritan.tdz.exception.DataAccessException;

/**
 * @author bozana
 *
 */
public interface UniqueValidator {
	
	/**
	 * @param entityName
	 * @param entityProperty
	 * @param value - value of the property (value for specified column in the table)
	 * @param siteCode - site code of the property of interest
	 * @param parentId TODO
	 * @param ignoreProperty - optional name of a property in which to ignore uniqueness check
	 * @param ignorePropertyValue - optional ignore property value
	 * @return:  true - value is unique
	 * 			 false - value is not unique
	 * @throws IllegalArgumentException 
	 * @throws DataAccessException 
	 */
	Boolean isUnique(String entityName, String entityProperty, Object value, String siteCode, Long parentId, String ignoreProperty, Object ignorePropertyValue) throws DataAccessException, ClassNotFoundException;
}
