/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.SavedItemData;

/**
 * @author prasanna
 *
 */
public class NonRackableSaveBehavior implements ItemSaveBehavior {
	
	@Autowired
	private ItemHome itemHome;
	
	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preValidateUpdate(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// Nothing to do at this time.
		
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preSave(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#postSave(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void postSave(Item item, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		boolean isUpdate = item != null && item.getItemId() > 0;
		Item origItem = isUpdate && SavedItemData.getCurrentItem() != null ? SavedItemData.getCurrentItem().getSavedItem() : null;
		if (null != item && null != item.getMountedRailLookup()) {
			long cabinetId = (null != item.getParentItem()) ? item.getParentItem().getItemId() : -1;
			long uPosition = item.getuPosition();
			long railsLkpValueCode = item.getMountedRailLookup().getLkpValueCode();
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, item, null);
		}
		if (isUpdate && null != origItem && null != item && 
				null != origItem.getParentItem() && null != item.getParentItem() && 
				null != origItem.getMountedRailLookup() && null != item.getMountedRailLookup() && 
				(origItem.getParentItem().getItemId() != item.getParentItem().getItemId() || 
				origItem.getuPosition() != item.getuPosition() || 
				origItem.getMountedRailLookup().getLkpValueCode() != item.getMountedRailLookup().getLkpValueCode())) {
			long cabinetId = (null != origItem.getParentItem()) ? origItem.getParentItem().getItemId() : -1;
			long uPosition = origItem.getuPosition();
			long railsLkpValueCode = origItem.getMountedRailLookup().getLkpValueCode();
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, item, null);
		}

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#canSupportDomain(java.lang.String[])
	 */
	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(ItItem.class.getName()) || name.equals(MeItem.class.getName())){
				canSupport = true;
			}
		}
		return canSupport;
	}

}
