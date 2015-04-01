/**
 * 
 */
package com.raritan.tdz.location.dao;

import com.raritan.tdz.exception.DataAccessException;

/**
 * This interface helps in executing any specific updates on the location objects.
 * Again this uses the IntroductionAdvisor and the actual implementation will be 
 * part of the generic DAO.
 * @author prasanna
 *
 */
public interface LocationUpdateDAO {
	
	/**
	 * This updates default site value for
	 * the all the other sites except the given locationId
	 * @param locationId
	 * @param defaultValue
	 * @return
	 */
	public int updateDefaultSiteExcludeCurrent(Boolean defaultValue, Long excludeLocationId) throws DataAccessException;

}
