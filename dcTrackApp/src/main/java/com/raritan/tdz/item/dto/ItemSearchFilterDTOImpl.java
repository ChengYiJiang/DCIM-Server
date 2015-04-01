/**
 * 
 */
package com.raritan.tdz.item.dto;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author prasanna
 *
 */
public class ItemSearchFilterDTOImpl implements ItemSearchFilterDTO {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchFilterDTO#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchFilterDTO#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchFilterDTO#setKey(java.lang.String)
	 */
	@Override
	public void setKey(String key) {
		this.key = key;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchFilterDTO#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchFilterDTO#getOperation()
	 */
	@Override
	public String getOperation() {
		return operation;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchFilterDTO#setOperation(java.lang.String)
	 */
	@Override
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	@NotBlank
	private String operation;
	
	@NotBlank
	private String key;
	
	@NotBlank
	private String value;
}
