package com.raritan.tdz.item.dto;

public interface ItemSearchFilterDTO {

	/**
	 * @return the operation
	 */
	public abstract String getOperation();

	/**
	 * @return the key
	 */
	public abstract String getKey();

	/**
	 * @return the value
	 */
	public abstract String getValue();

	/**
	 * @param key the key to set
	 */
	public abstract void setKey(String key);

	/**
	 * @param value the value to set
	 */
	public abstract void setValue(String value);

	/**
	 * @param operation the operation to set
	 */
	public abstract void setOperation(String operation);
}