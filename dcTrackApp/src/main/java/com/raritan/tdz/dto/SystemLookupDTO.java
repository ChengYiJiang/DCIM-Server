/**
 * 
 */
package com.raritan.tdz.dto;

/**
 * @author prasanna
 *
 */
public class SystemLookupDTO {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dto.SystemLookupDTO#getLkpValueCode()
	 */
	public Long getData() {
		return lkpValueCode;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dto.SystemLookupDTO#getLkpValue()
	 */
	public String getLabel() {
		return lkpValue;
	}
	/**
	 * @param lkpValueCode the lkpValueCode to set
	 */
	public void setData(Long lkpValueCode) {
		this.lkpValueCode = lkpValueCode;
	}
	/**
	 * @param lkpValue the lkpValue to set
	 */
	public void setLabel(String lkpValue) {
		this.lkpValue = lkpValue;
	}
	
	private Long lkpValueCode;
	private String lkpValue;
}
