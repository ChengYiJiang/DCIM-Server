package com.raritan.tdz.chassis.home;

import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.chassis.home.ChassisItemImpl;

public class NetworkChassisItemImpl extends ChassisItemImpl {

	@Override
	protected void postSaveItem(Long itemId,Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws DataAccessException{
		super.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
		ItItem item = (ItItem)itemDomain;
		if (item != null && item.getPropagateFields()){
				propagateValuesToBlades( itemId, item);
		}//if
	}

	
	private void replicateProperties( ItItem source, ItItem dest){
		if( source != null & dest != null &&
				null != dest.getClassLookup() && dest.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.NETWORK)){
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
	
	private void propagateValuesToBlades(Long itemId, ItItem chassis) throws DataAccessException {
		int numBlades = getChildItemCount();
		if( numBlades > 0){
			List<Item> blades = getChildItems();
			for( Item blade : blades){
				replicateProperties(chassis, (ItItem)blade);
			}//for
		}//if
	}
	
	public NetworkChassisItemImpl(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
}
