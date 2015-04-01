/**
 * 
 */
package com.raritan.tdz.util;

import java.util.Map;

import org.springframework.validation.Errors;


/**
 * @author bozana
 *
 */
public interface DCTColumnsSchema {
	
	/**
	 * create a map containing pairs: uiId and max length of property
	 */
	void createUiIDPropertyLengthMap();
	
	
	/**
	 *
	 * @return map containitn pairs: uiID, max_length
	 * where format max_length is returned as a String whose format
	 * depends on data type according to the following rules:
	 * 		
	 * 		data type:           format:
	 * -----------------------------------------------
	 * 		bigint             "precision.radix.scale"
	 *      smallint           "precision.radix.scale"
	 *      numeric            "precision.radix.scale"
	 *      integer            "precision.radix.scale"
	 *      character varying  "max_length"
	 *      text               " "
	**/
	public Map<String, String> getUiIdLenthMap();

	/**
	 * 
	 * @param uiId 
	 * @return length of the property for specified uiId
	 */
	public String getPropertyLength( String uiId );
	
	/**
	 * Validate the given double value against the database schema
	 * @param uiId
	 * @param value
	 * @param errors
	 */
	
	public void validate(String uiId, Double value, Errors errors);
	
	/**
	 * Validate the given integer value against the database schema
	 * @param uiId
	 * @param value
	 * @param errors
	 */
	public void validate(String uiId, Integer value, Errors errors);
	
	/**
	 * Validate the given string value against the database schema.
	 * @param uiId
	 * @param value
	 * @param errors
	 */
	public void validate(String uiId, String value, Errors errors);
}
