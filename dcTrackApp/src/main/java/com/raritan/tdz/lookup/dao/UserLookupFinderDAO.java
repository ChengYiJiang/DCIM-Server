/**
 * 
 */
package com.raritan.tdz.lookup.dao;

import java.util.List;

import com.raritan.tdz.domain.LkuData;

/**
 * This provides finder methods for user lookup (dct_lku_data)
 * <p>Note: Implementation will be found in the DAOImpl via AOP Introduction</p>
 * @see daos.xml
 * @author prasanna
 *
 */
public interface UserLookupFinderDAO {

	/**
	 * Find the user lookup by id
	 * @param id
	 * @return
	 */
	public List<LkuData> findById(Long id);
	
	/**
	 * Find the user lookup by type (lku_type_name)
	 * @param lkpType
	 * @return
	 */
	public List<LkuData> findByLkpType(String lkpType);
	
	/**
	 * Find the user lookup by attribute
	 * @param lkpAttribute
	 * @return
	 */
	public List<LkuData> findByLkpAttribute(String lkpAttribute);
	
	/**
	 * Find the user lookup by both type and attribute
	 * @param lkpAttribute
	 * @param lkpType
	 * @return
	 */
	public List<LkuData> findByLkpAttributeAndType(String lkpAttribute, String lkpType);
	
	/**
	 * Find the user lookup by type and the lkpValueCode (from lks data)
	 * @param lkpType
	 * @param lkpValueCode
	 * @return
	 */
	public List<LkuData> findByLkpTypeAndLkpValueCode(String lkpType, Long lkpValueCode);
	
	/**
	 * Find the user lookup by lkuValue
	 * @param lkuValue
	 * @return
	 */
	public List<LkuData> findByLkpValue(String lkuValue);
	
	/**
	 * Find case insensitive user lookup by lkuTypeName and lkuValue
	 * @param lkuValue
	 * @return
	 */
	
	public List<LkuData> findByLkpValueAndTypeCaseInsensitive(String lkuValue,  String lkuTypeName);
	/**
	 * Find case insensitive user lookup by lkuTypeName and lkuValue, lkpAttribute
	 * @param lkuValue
	 * @return
	 */
	public List<LkuData> findByLkpValueLkpAttributeAndTypeCaseInsensitive(String lkuValue, String lkpAttribute, String lkuTypeName);
	
}
