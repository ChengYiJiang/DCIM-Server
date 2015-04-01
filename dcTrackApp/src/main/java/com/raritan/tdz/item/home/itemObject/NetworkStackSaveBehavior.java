/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class NetworkStackSaveBehavior implements ItemSaveBehavior {

	@Autowired
	private ItemDAO itemDao;
	
	ItItem item;
	
	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preValidateUpdate(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// Nothing to do at this time.
		
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preSave(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preSave(Item itemDomain, Object... additionalArgs)
			throws BusinessValidationException {
		ItItem item = (ItItem)itemDomain;
		Item cracItem = item.getCracNwGrpItem();
		List<Item> siblings = new ArrayList<Item>();
		
		if (cracItem != null) siblings = (List<Item>) itemDao.getNetworkStackItems(cracItem.getItemId());
		
		if (siblings.size() == 0){
			item.setGroupingName(item.getItemName());
			item.setCracNwGrpItem(item);
			item.setNumPorts(1);
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#postSave(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void postSave(Item item, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		//propagate OS, OSI layer and other fields to all stacks
		this.item = (ItItem)item;
		if (item != null && item.getPropagateFields() && item.getCracNwGrpItem() != null){
			propagateValuesToStacks(item.getCracNwGrpItem().getItemId(), this.item);
		}//if

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#canSupportDomain(java.lang.String[])
	 */
	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(ItItem.class.getName())){
				canSupport = true;
			}
		}
		return canSupport;
	}
	
	private void replicateProperties( ItItem source, ItItem dest){
		if( source != null & dest != null &&
				null != dest.getClassLookup() && dest.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.NETWORK) ){
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
	
	private void propagateValuesToStacks(Long itemId, ItItem parent) throws DataAccessException {
		List<Item> siblings = (List<Item>) itemDao.getNetworkStackItems(itemId);
		int numChildren = siblings.size();
		if( numChildren > 0){
			
			for( Item sib : siblings){
				//TODO: For some reason the children that we get from hibernate does not have actual objects. It has
				//TODO: some sort of a proxy object (javassit objects)! However the data within it are just fine
				//TODO: For now force fetch the item based on the itemId. We need to fix this in the future
				//TODO: to make it more efficient.
				if(parent.getItemId() == sib.getItemId()) continue;
				
				ItItem x = (ItItem) itemDao.loadItem(sib.getItemId());
				replicateProperties(parent, x);
				itemDao.saveItem(x);
			}//for
		}//if
	}

}
