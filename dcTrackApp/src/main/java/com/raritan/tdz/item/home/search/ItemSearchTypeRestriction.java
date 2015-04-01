/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.InvocationTargetException;

import org.hibernate.Criteria;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.SystemException;

/**
 * @author prasanna
 * This interface is used to add restriction based on the type of searchKey
 */
public interface ItemSearchTypeRestriction {
	void addRestrictionForOperator(Criteria criteria, String alias, 
				String filterKey, String filterValue, String operator)
						throws SystemException, BusinessValidationException, 
						ClassNotFoundException, InstantiationException, 
						IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException;
}
