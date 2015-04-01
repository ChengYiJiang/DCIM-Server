package com.raritan.tdz.item.itemState;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;

public class NewStateRoleValidator extends RoleValidatorBase {

	@Override
	public boolean canTransition(Item item, UserInfo userInfo) {
		boolean userPermitted = false;
		Long itemId = item.getItemId();
		if( itemId > 0 ){
			//this is edit option
			userPermitted = isPermittedUser(item, userInfo);
		}else{
			//this is new item operation - anybody can do it except viewer
			userPermitted = isNonViewer(userInfo);
		}
		return userPermitted;
	}

}
