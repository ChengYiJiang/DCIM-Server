package com.raritan.tdz.item.itemState;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;

public class InstallStateRoleValidator extends RoleValidatorBase {
	final static long thisState = SystemLookup.ItemStatus.INSTALLED;
	
	@Override
	public boolean canTransition(Item item, UserInfo userInfo) {
		
		//When moving to the same state as current one, generic rules about permissions apply
		if(SavedItemData.getCurrentItemStatusValueCode() != null && SavedItemData.getCurrentItemStatusValueCode().longValue() == thisState ){
			return super.isPermittedUser(item, userInfo);
		}
		boolean userPermitted = false;
        if( userInfo.isAdmin()){
        	userPermitted = true;
        }
        log_debug("returning " + userPermitted);
        return userPermitted;
	}

}
