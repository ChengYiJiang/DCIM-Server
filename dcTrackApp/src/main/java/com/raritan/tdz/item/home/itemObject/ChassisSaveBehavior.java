/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author prasanna
 *
 */
public class ChassisSaveBehavior implements ItemSaveBehavior {
	
	@Autowired
	private ChassisHome chassisHome;
	
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
		//Nothing to do here.
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#postSave(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void postSave(Item item, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		ItItem itItem = (ItItem) item;
/*		boolean updateGroupName = (null == origItem) || 
				(null != origItem && origItem.getGroupingName().equals(itItem.getGroupingName()));*/
		if (null != itItem) {
			// Chassis model could have changed, make sure to update the layout information
			chassisHome.updateSlotNumberForBladesInChassis(itItem.getItemId());
			chassisHome.updateChassisGroupName(itItem.getItemId());
			chassisHome.updateCabinetAndLocationForBladesInChassis(itItem.getItemId());
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
			if (name.equals(ItItem.class.getName())){
				canSupport = true;
			}
		}
		return canSupport;
	}

}
