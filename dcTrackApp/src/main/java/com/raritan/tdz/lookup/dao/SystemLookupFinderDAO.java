/**
 * 
 */
package com.raritan.tdz.lookup.dao;

import java.util.List;

import com.raritan.tdz.domain.LksData;

/**
 * This DAO provides finder methods for system lookup (dct_lks_data)
 * <p>Note: Implementation will be found in the DAOImpl via AOP Introduction</p>
 * @author prasanna
 * @see daos.xml
 */
public interface SystemLookupFinderDAO {
	/**
	 * Find system lookup by Id
	 * @param id
	 * @return
	 */
	public List<LksData> findById(Long id);
	
	/**
	 * Find system lookup by lookup value code
	 * @param lkpValueCode
	 * @return
	 */
	public List<LksData> findByLkpValueCode(Long lkpValueCode);
	
	/**
	 * Find system lookup by lkpType (There may be more than one returned)
	 * @param lkpType
	 * @return
	 */
	public List<LksData> findByLkpType(String lkpType);
	
	/**
	 * Find system lookup by lkpValue (There may be more than one returned)
	 * @param lkpValue
	 * @return
	 */
	public List<LksData> findByLkpValue(String lkpValue);
	
	
	/**
	 * Find system lookup by lkp type name and lkp value
	 * @param lkpTypeName
	 * @param lkpValue
	 * @return
	 */
	public List<LksData> findByLkpTypeNameAndLkpValue(String lkpTypeName, String lkpValue);
	/**
	 * Find system lookup by lkpValue and lkpType (There may be more than one returned)
	 * @param lkpValue
	 * @param lkpType
	 * @return
	 */
	public List<LksData> findByLkpValueAndType(String lkpValue, String lkpType);
	
	/**
	 * Find case insensitive system lookup by lkpValue and lkpType (There may be more than one returned)
	 * @param lkpValue
	 * @param lkpType
	 * @return
	 */
	public List<LksData> findByLkpValueAndTypeCaseInsensitive(String lkpValue, String lkpType);
}
