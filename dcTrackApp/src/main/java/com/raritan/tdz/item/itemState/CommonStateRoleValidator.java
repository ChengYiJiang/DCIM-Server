package com.raritan.tdz.item.itemState;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.home.SavedItemData;

public class CommonStateRoleValidator extends RoleValidatorBase {
	long thisState = -1L;
	
	
	
	public long getThisState() {
		return thisState;
	}



	public void setThisState(long thisState) {
		this.thisState = thisState;
	}



	@Override
	public boolean canTransition(Item item, UserInfo userInfo) {
		
		//When moving to the same state as current one, generic rules about permissions apply
		if(SavedItemData.getCurrentItemStatusValueCode() != null && SavedItemData.getCurrentItemStatusValueCode().longValue() == getThisState() ){
			return super.isPermittedUser(item, userInfo);
		}
		
		return userInfo.isAdmin();
		
	}

}
