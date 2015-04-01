/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.ItemDeleteHelper;

/**
 * @author prasanna
 *
 */
public class ItemDeleteCommonBehavior implements ItemDeleteBehavior {
	
	@Autowired
	ItemDeleteHelper itemDelete;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#deleteItem(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteItem(Item item) throws Throwable {
		itemDelete.deleteItem(item);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#preDelete(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void preDelete(Item item) throws BusinessValidationException {
		//Nothing to do here

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#postDelete()
	 */
	@Override
	public void postDelete() throws BusinessValidationException {
		// Nothing to do here.

	}

}
