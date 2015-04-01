/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import com.raritan.tdz.domain.Item;

/**
 * @author prasanna
 * This is a factory that produces changeModel objects
 * based on class/subclass of existing item to item to be 
 * saved.
 */
public interface ChangeModelFactory {
	/**
	 * Get the appropriate changeModel object to perform 
	 * any business logic for changing the model
	 * The change model is based on the Class/Subclass of 
	 * item in db to to item to save. There may be cases where
	 * you may get special changeModel that will perform additional
	 * checks against mounting.
	 * @param itemInDB
	 * @param itemToSave
	 * @return
	 */
	public ChangeModel getChangeModel(Item itemInDB, Item itemToSave);

	/**
	 * Get the appropriate changeModel object to perform 
	 * any business logic for changing the model
	 * The change model is based on the Class/Subclass of 
	 * item in db to to item to save. There may be cases where
	 * you may get special changeModel that will perform additional
	 * checks against mounting.
	 * @param itemInDBMountingFormFactorValue
	 * @param itemToSaveMountingFormFactorValue
	 * @return
	 */	
	public ChangeModel getChangeModel(Long itemInDBMountingFormFactorValue, Long itemToSaveMountingFormFactorValue);
}
