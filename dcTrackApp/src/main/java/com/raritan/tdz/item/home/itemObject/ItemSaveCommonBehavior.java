package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

public class ItemSaveCommonBehavior implements ItemSaveBehavior {

	@Autowired(required=true)
	private ItemNameGenerator itemNameGenerator;

	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {

		if (itemNameGenerator.shouldGenerateName(item)) {
			itemNameGenerator.generateName(item);
		}

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
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// supported for all kind of items
		return true;
	}

}
