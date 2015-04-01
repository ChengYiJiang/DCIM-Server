/**
 * 
 */
package com.raritan.tdz.item.home;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.util.UniqueValidatorParentRestriction;

/**
 * @author prasanna
 *
 */
public class PowerPanelUniqueValidatorParentRestriction implements
		UniqueValidatorParentRestriction {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.UniqueValidatorParentRestriction#addParentRestriction(org.hibernate.Criteria, java.lang.Long)
	 */
	@Override
	public void addParentRestriction(Criteria criteria, Long parentId) {
		criteria.createAlias("parentItem", "parentItem");
		criteria.add(Restrictions.eq("parentItem.itemId", parentId));

	}

}
