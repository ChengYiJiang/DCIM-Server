/**
 * 
 */
package com.raritan.tdz.dctimport.processors;

import com.raritan.tdz.dctimport.dto.DCTImport;

/**
 * @author prasanna
 * Process the import line items
 */
public interface ImportProcessor {
	
	/**
	 * Process operation to the import line items
	 * @param importDTO
	 * @throws Exception
	 */
	public void process(DCTImport importDTO) throws Exception;
}
