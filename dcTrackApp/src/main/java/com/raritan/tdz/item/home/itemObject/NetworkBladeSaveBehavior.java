/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * This is the save behavior for the networkBladeServer.
 * @author prasanna
 *
 */
public class NetworkBladeSaveBehavior implements ItemSaveBehavior {
	
	@Autowired
	private ChassisHome chassisHome;
	
	private Item item;
	
	@Autowired
	private BladeSaveBehavior saveBehavior;

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
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// Nothing to be done here

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#postSave(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void postSave(Item item, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		//First ensure that the BladeServerBehavior is called
		if (saveBehavior != null && saveBehavior.canSupportDomain(item.getClass().getName())){
			saveBehavior.postSave(item, null, null);
		}
		
		ItItem blade = (ItItem)item;
		this.item = item;
		if( blade != null && blade.getPropagateFields()){
			propagateToChassis(blade);
			propagateToBlades(blade);
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

	//Private methods
	private void replicateProperties( ItItem source, ItItem dest){
		if( source != null && dest != null &&
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
	
	private void propagateToBlades( ItItem bladeSrc) throws DataAccessException{
		ChassisHome chassis = chassisHome;
		if( chassis == null || bladeSrc.getBladeChassis() == null) return;
		
		List<ItItem> blades = (List<ItItem>)chassis.getAllBladesForChassis(bladeSrc.getBladeChassis().getItemId());
		for( ItItem bladeDst : blades){
			replicateProperties( bladeSrc, bladeDst);
		}//for
		
	}
	
	private void propagateToChassis( ItItem blade ){
		ItItem chassis = (ItItem)blade.getBladeChassis();
		if( chassis != null ) replicateProperties(blade, chassis);
	}
}
