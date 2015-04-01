/**
 * 
 */
package com.raritan.tdz.item.itemState;

/**
 * @author prasanna
 * The only purpose of this interface is to check for the value 
 * of a mandatory field against a range.
 */
public interface MandatoryFieldValidatorRangeCheck {
	/**
	 * Check the clientValue against the range.
	 * @param clientValue
	 * @return
	 */
	public boolean checkRange(Object clientValue);
	
	/**
	 * Gets the default label for a specific mandatory field
	 * @return
	 */
	public String getDefaultLabel();
	
	/**
	 * Gets the error code that is specific to the mandatory field.
	 * This is esp useful when you want to have a different error 
	 * for different fields. If given null, the consumer should assume
	 * the error code
	 * @return
	 */
	public String getErrorCode();
}
