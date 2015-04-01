package com.raritan.tdz.move.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.item.request.ItemRequest.ItemRequestType;

/**
 * 
 * @author bunty
 *
 */
public class ItemMoveDeletePlannedCircuits implements ItemSaveBehavior {

	@Autowired
	private ItemSaveBehavior itemDeletePlannedCircuits;
	
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		if (null == item.getItemToMoveId()) { //do nothing
			return;
		}
		
		itemDeletePlannedCircuits.postSave(item, sessionUser, item.getItemToMoveId(), ItemRequestType.moveItem);

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}

}
