/**
 * 
 */
package com.raritan.tdz.dctimport.dto;

import java.util.Map;

/**
 * @author prasanna
 *
 */
public interface DCTImportHeaderMapCache {
	/**
	 * Get the header map from cache
	 * @return
	 */
	public Map<String, String> getHeaderMap();

	/**
	 * get the reverse header map i.e. variable name as key and header as the value
	 * @return
	 */
	public Map<String, String> getReverseHeaderMap();
	
}
