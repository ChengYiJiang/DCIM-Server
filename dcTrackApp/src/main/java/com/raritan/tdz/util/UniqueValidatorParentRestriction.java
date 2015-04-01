/**
 * 
 */
package com.raritan.tdz.util;

import org.hibernate.Criteria;

/**
 * This interface can be used to add restrictions to the 
 * uniqueness criteria based on the parent. This way
 * one can further filter the uniqueness check to the parent
 * @author prasanna
 *
 */
public interface UniqueValidatorParentRestriction {
	/**
	 * Add further restriction based on the parent id.
	 * Note that this can be used not only for items, but also 
	 * anything that has a parent associated with it!
	 * @param criteria
	 * @param parentId
	 */
	void addParentRestriction(Criteria criteria, Long parentId);
}
