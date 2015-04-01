/**
 * 
 */
package com.raritan.tdz.dctimport.dto;

import java.util.List;

import com.raritan.tdz.dctimport.integration.exceptions.HeaderNotFoundException;
import com.raritan.tdz.dctimport.integration.exceptions.IncorrectHeaderException;

/**
 * This interface must be implemented by all the 
 * import beans
 * @author prasanna
 *
 */
public interface DCTImport {
	/**
	 * Returns import operation column
	 * @return
	 */
	public String getOperation();
	
	/**
	 * Returns the import object type
	 * @return
	 */
	public String getObjectType();
	
	/**
	 * Based on the header row, returns the names
	 * to be set to the tokenizer and the lineMapper
	 * If any of the columns do not match with this object
	 * throws an exception.
	 * @param headerRow
	 * @param originalHeaderRow Original Header row is used to report errors in the header
	 * @return
	 * @throws Exception
	 */
	public List<String> getNames(String headerRow, String originalHeaderRow) throws IncorrectHeaderException,HeaderNotFoundException;
	
	/**
	 * Given the original Header Column Name, get the corresponding
	 * fieldName.
	 * @param originalHeaderName
	 * @return
	 */
	public String getFieldName(String originalHeaderName);
}
