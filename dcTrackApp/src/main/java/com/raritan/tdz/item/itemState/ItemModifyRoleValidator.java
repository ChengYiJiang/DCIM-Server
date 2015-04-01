package com.raritan.tdz.item.itemState;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.item.home.SavedItemData;


public class ItemModifyRoleValidator extends RoleValidatorBase {
	
	@Override
	public boolean canTransition(Item item, UserInfo userInfo) {
		boolean userPermitted = false;
		
		if (item != null){
			Long itemId = item.getItemId();
			if(itemId > 0 ){
				Users adminUser = item.getItemServiceDetails() != null ? item.getItemServiceDetails().getItemAdminUser() : null;
				LkuData teamLookup = item.getItemServiceDetails() != null ? item.getItemServiceDetails().getItemAdminTeamLookup() : null;
				userPermitted = isPermittedUser(userInfo, adminUser,
						teamLookup);
			}
		}
		return userPermitted;
	}

}