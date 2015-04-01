package com.raritan.tdz.chassis.home;
import java.util.List;

import org.hibernate.SessionFactory;

import com.raritan.tdz.chassis.home.BladeItemImpl;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;


public class NetworkBladeItemImpl extends BladeItemImpl {
	@Override
	protected void postSaveItem(Long itemId, Object itemDomain, UserInfo sessionUser, Boolean isUpdate){
		super.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
		ItItem blade = (ItItem)itemDomain;
		if( blade != null && blade.getPropagateFields()){
			propagateToChassis(blade);
			propagateToBlades(blade);
		}//if
	}

	private void replicateProperties( ItItem source, ItItem dest){
		if( source != null && dest != null && 
				null != dest.getClassLookup() && dest.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.NETWORK)) {
			dest.setItemAlias(item.getItemAlias());
			ItemServiceDetails isd = dest.getItemServiceDetails();
			isd.setFunctionLookup(item.getItemServiceDetails().getFunctionLookup());
			isd.setItemAdminTeamLookup(item.getItemServiceDetails().getItemAdminTeamLookup());
			isd.setItemAdminUser(item.getItemServiceDetails().getItemAdminUser());
			isd.setPurposeLookup(item.getItemServiceDetails().getPurposeLookup());
			isd.setDepartmentLookup(item.getItemServiceDetails().getDepartmentLookup());
			dest.setItemServiceDetails(isd);
			dest.setOsiLayerLookup(source.getOsiLayerLookup());
			dest.setOsLookup(source.getOsLookup());
		}//if
	}
	
	public void propagateToBlades( ItItem bladeSrc){
		ChassisHome chassis = getChassisHome();
		if( chassis == null || bladeSrc.getBladeChassis() == null) return;
		try {
			List<ItItem> blades = (List<ItItem>)chassis.getAllBladesForChassis(bladeSrc.getBladeChassis().getItemId());
			for( ItItem bladeDst : blades){
				replicateProperties( bladeSrc, bladeDst);
			}//for
		} catch (DataAccessException e) {		
			e.printStackTrace();
		}//catch
	}
	
	public void propagateToChassis( ItItem blade ){
		//must load chassis item to get all fields, otherwise will fail due to lazy loading
		ItItem chassis = (ItItem)loadItem(ItItem.class, blade.getItemId());
		if( chassis != null ) replicateProperties(blade, chassis);
	}
	
	public NetworkBladeItemImpl(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
}
	
