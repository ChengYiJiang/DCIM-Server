/**
 *
 */
package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.ItemHome;

/**
 * @author prasanna
 *
 */
public class NRItemDeleteBehavior implements ItemDeleteBehavior {

	@Autowired
	private ItemHome itemHome;
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#deleteItem(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		/* update the shelf position of all the items @ this cabinet and u-position */
		long cabinetId = (null != item.getParentItem()) ? item.getParentItem().getItemId() : -1;
		long uPosition = item.getuPosition();

		if(cabinetId > 0 && item.getMountedRailLookup() != null){
			long railsLkpValueCode = item.getMountedRailLookup().getLkpValueCode();
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, null, null);
		}

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#preDelete(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void preDelete(Item item) throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#postDelete()
	 */
	@Override
	public void postDelete() throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

}
