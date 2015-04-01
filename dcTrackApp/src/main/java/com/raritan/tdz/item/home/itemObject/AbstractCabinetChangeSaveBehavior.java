package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.util.ValueIdDTOHolder;

public abstract class AbstractCabinetChangeSaveBehavior implements ItemSaveBehavior {

	@Autowired
	ItemDAO itemDAO;
	
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		Long cabinetId = null;
		Object cmbCabinet = ValueIdDTOHolder.getCurrent().getValue("cmbCabinet");
		if (cmbCabinet instanceof Integer) 
			cabinetId = ((Integer)cmbCabinet).longValue();
		else if (cmbCabinet instanceof Long)
			cabinetId = (Long)cmbCabinet;
		if (cabinetId != null && item != null && item.getItemId() > 0 && cabinetId > 0) {
			if (itemDAO.isCabinetChanged (item.getItemId(), cabinetId) == true) {
				clearFields(item);
			}
		}
	}

	protected abstract void clearFields(Item item);

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
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(Item.class.getName()) || 
					name.equals(ItItem.class.getName()) ||
					name.equals(MeItem.class.getName())){
				canSupport = true;
			}
		}
		return canSupport;
	}
}
