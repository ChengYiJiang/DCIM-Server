/**
 * 
 */
package com.raritan.tdz.location.dao;

import java.util.List;

import com.raritan.tdz.dao.DAOAliasToBeanType;
import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.DataCenterLocationDetails;

/**
 * A location finder DAO that can be used via LocationDAO (as this is proxied via spring AOP Introduction)
 * @author prasanna
 * 
 */
public interface LocationFinderDAO {
	/**
	 * Get location details based on the id.
	 * <p>Remember that there should be only one element in the list</p>
	 * @param id - Location Id
	 * @return
	 */
	public List<DataCenterLocationDetails> findById(Long id);
	
	/**
	 * Get location details based on the id.
	 * <p>This will be returning a readonly location detail</p>
	 *  <p>Remember that there should be only one element in the list</p>
	 * @param id - Location Id
	 * @return
	 */
	public List<DataCenterLocationDetails> findByIdReadOnly(Long id);

	/**
	 * Fetch the location details based on id
	 * <p>This will fetch the location details as a transient object (detached to session)</p>
	 * @param id - Location Id
	 * @return
	 */
	public List<DataCenterLocationDetails> fetchById(Long id);
	
	/**
	 * Find the location code by Id
	 * <p>This will return you one location code based on id</p>
	 * @param id - Location Id
	 * @return
	 */
	public List<String> findLocationCodeById(Long id);
	
	/**
	 * Find the locations based on hierarchy lookup value code.
	 * @param hierarchyLkpValueCode
	 * @return
	 */
	public List<DataCenterLocationDetails> findLocationsByHierarchy(Long hierarchyLkpValueCode);
	
	/**
	 * Returns the default location.
	 * @return
	 */
	public List<DataCenterLocationDetails> findDefaultLocation();
	
	/**
	 * Returs List of ids of all locations that are visible to the end user, i.e.
	 * those whose code is not 'Floor' and not 'Site' and not 'Building' 
	 * @return
	 */
	public List<Long> findAllVisibleLocationsId();
}
