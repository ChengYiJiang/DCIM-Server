/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @author prasanna
 * This interface handles the business logic behind 
 * change of model of an item
 */
public interface ChangeModel {
	/**
	 * This will handle the business logic behind changing the model of
	 * existing item represented by itemInDB to the model of itemToSave 
	 * @param itemInDB
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void change(Item itemInDB, Item itemToSave) throws BusinessValidationException;
	
	/**
	 * This can be basically used to initialize the itemToSave and/or itemInDB where
	 * required before change occurs. We can call this just before validations of the item
	 * so that the itemToSave can be initialized.
	 * @param itemInDB
	 * @param itemToSave
	 * @param additionalParams TODO
	 */
	public void init(Item itemInDB, Item itemToSave, Object additionalParams);
}
