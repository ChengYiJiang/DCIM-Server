/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * This will take care of any specific business logic 
 * needed to handle the delete of an item functionality
 * @author prasanna
 *
 */
public interface ItemDeleteBehavior {
	/**
	 * Performs delete operation. This could be one of the
	 * business logic that has to be taken care before deleting
	 * actual item. 
	 * <p><b>Note</b> that this will delete only items in "Planned"
	 * state.</p>
	 * @param item
	 * @throws BusinessValidationException
	 * @throws Throwable 
	 */
	public void deleteItem(Item item) throws BusinessValidationException, Throwable;
	
	/**
	 * Perform any pre-delete operations here
	 * @param item
	 * @throws BusinessValidationException
	 */
	public void preDelete(Item item) throws BusinessValidationException;
	
	/**
	 * Perform any post-delete operations here
	 * @throws BusinessValidationException
	 */
	public void postDelete() throws BusinessValidationException;
}
