package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;

/**
 * update the status the passive items in the cabinet to be the same as the cabinet
 * @author bunty
 *
 */
public class CabinetPassiveChildrenSaveBehavior implements ItemSaveBehavior {

	@Autowired
	private ItemDAO itemDAO;
	
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
		
		itemDAO.propagateParentStatusToPassiveChildren(item.getItemId(), item.getStatusLookup().getLksId());

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(CabinetItem.class.getName())){
				canSupport = true;
			}
		}

		return canSupport;
	}

}
