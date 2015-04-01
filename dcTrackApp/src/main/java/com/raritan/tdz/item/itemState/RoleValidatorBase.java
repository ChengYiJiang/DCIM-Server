package com.raritan.tdz.item.itemState;

import org.apache.log4j.Logger;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.item.home.SavedItemData;

public class RoleValidatorBase implements RoleValidator {
	private final Logger log = Logger.getLogger(RoleValidatorBase.class);
	
	protected boolean isNonViewer(UserInfo userInfo){
		boolean userPermitted = false;
		if( null != userInfo && (userInfo.isAdmin() || userInfo.isManager() || userInfo.isMember())){
			userPermitted = true;
		}
		return userPermitted;
	}
	
	protected boolean isManager(Item item, UserInfo userInfo){
		boolean userPermitted = false;
		if( null != userInfo && userInfo.isManager()){
			userPermitted = true;
		}
		return userPermitted;
	}
	
	protected void log_debug(Object message){
		if(log.isDebugEnabled()){
			log.debug(message);
		}
	}
	
	protected boolean isPermittedUser(Item item, UserInfo userInfo){
		Users currSysAdmin = SavedItemData.getCurrentItemAdminUser();
		LkuData currAdminTeam = SavedItemData.getCurrentItemAdminTeamLookup();

		return isPermittedUser(userInfo, currSysAdmin, currAdminTeam);
	}
	
	@Override
	public boolean isPermittedUser(UserInfo userInfo, Users currSysAdmin,
			LkuData currAdminTeam) {

		//viewer is never permitted
		if( null != userInfo && userInfo.isViewer()){
			log_debug("viewer -> not permitted");
			return false;
		}

		//gatekeeper (admin) is always permitted
		if( null != userInfo && userInfo.isAdmin()){
			log_debug("admin -> permitted");
			return true;
		}
		
		//there is no sysadmin and no admin team, any non viewer can access
		if(currSysAdmin == null && currAdminTeam == null ){
			log_debug("currSysAdmin NOT set and currAdminTeam NOT set, checking if nonviewer");
			return isNonViewer(userInfo);
		}
		
		//if sysAdmin is set, and curr user is sys admin -> has permission
		if( currSysAdmin != null &&  null != userInfo && userInfo.getUserId().equals(currSysAdmin.getId().toString())){
			log_debug("currSysAdmin set and user matches => user permitted");
			return true;
		}
		
		if( currAdminTeam != null && null != userInfo && userInfo.getTeamLookup() != null ){ //team owner is set
			log_debug("currAdminTeam set and userInfo.getTeamLookup() set, check more...");
			//adminTeam is set and sysAdmin is set and user belongs to the team and he is mgr -> has permission
			if( currSysAdmin != null ){ //and sys admin is set
				log_debug("currSysAdmin set, check more...");
				
				if(userInfo.getTeamLookup().getLkuId().equals(currAdminTeam.getLkuId()) && userInfo.isManager()) {
					log_debug("this is team manager -> permitted");
					return true; 
				}
			}else{ //there is no sys admin, but there is admin team, so any team member can access
				if(userInfo.getTeamLookup().getLkuId().equals(currAdminTeam.getLkuId())){
					
					log_debug("there is no sys admin, but there is admin team, any team member can access -> permitted"); 
					return true;
				}
			}
		}
		log_debug("Nothing matched, not permitted");
		return false;
	}
	
	
	@Override
	public boolean canTransition(Item item, UserInfo userInfo) {
		boolean userPermitted = false;
        if( userInfo.isAdmin()){
        	log_debug("This is admin -> permitted");
        	userPermitted = true;
        }
        log_debug("returning" + userPermitted);
        return userPermitted;
	}

}
