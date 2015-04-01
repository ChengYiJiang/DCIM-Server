package com.raritan.tdz.item.itemState;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.cmn.Users;

public interface RoleValidator {
	
	boolean canTransition( Item item, UserInfo user);

	/**
	 * check if the user 'userInfo' is permitted for the given sys admin and admin team
	 * @param userInfo
	 * @param currSysAdmin
	 * @param currAdminTeam
	 * @return
	 */
	boolean isPermittedUser(UserInfo userInfo, Users currSysAdmin,
			LkuData currAdminTeam);

}
