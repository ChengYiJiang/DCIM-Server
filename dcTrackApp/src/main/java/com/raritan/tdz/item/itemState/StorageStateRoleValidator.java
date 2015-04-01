package com.raritan.tdz.item.itemState;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;

public class StorageStateRoleValidator extends RoleValidatorBase {
	final static long thisState = SystemLookup.ItemStatus.STORAGE;
	
	@Override
	public boolean canTransition(Item item, UserInfo userInfo) {
		//When moving to the same state as current one, generic rules about permissions apply
		if(SavedItemData.getCurrentItemStatusValueCode() != null && SavedItemData.getCurrentItemStatusValueCode().longValue() == thisState ){
			return super.isPermittedUser(item, userInfo);
		}
		boolean retval =  isPermittedUser(item, userInfo);
		log_debug("returning retval=" + retval);
		return retval;
	}

}
