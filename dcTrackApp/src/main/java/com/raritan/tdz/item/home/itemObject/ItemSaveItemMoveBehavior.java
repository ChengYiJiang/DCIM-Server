package com.raritan.tdz.item.home.itemObject;

import java.util.List;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

public class ItemSaveItemMoveBehavior implements ItemSaveBehavior {

	private List<ItemSaveBehavior> itemSaveMoveBehaviors;
	
	public List<ItemSaveBehavior> getItemSaveMoveBehaviors() {
		return itemSaveMoveBehaviors;
	}

	public void setItemSaveMoveBehaviors(
			List<ItemSaveBehavior> itemSaveMoveBehaviors) {
		this.itemSaveMoveBehaviors = itemSaveMoveBehaviors;
	}

	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {

		for (ItemSaveBehavior itemMoveBehavior: itemSaveMoveBehaviors) {
			itemMoveBehavior.preValidateUpdate(item, additionalArgs);
		}
		
	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		
		for (ItemSaveBehavior itemMoveBehavior: itemSaveMoveBehaviors) {
			itemMoveBehavior.preSave(item, additionalArgs);
		}

	}
	
	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		for (ItemSaveBehavior itemMoveBehavior: itemSaveMoveBehaviors) {
			itemMoveBehavior.postSave(item, sessionUser, additionalArgs);
		}

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {

		boolean canSupport = true;

		for (ItemSaveBehavior itemMoveBehavior: itemSaveMoveBehaviors) {
			canSupport &= itemMoveBehavior.canSupportDomain(domainObjectNames);
		}

		return canSupport;
	}

}
