package com.raritan.tdz.item.json;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelChassis;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;



public class BasicBladeItemInfo extends BasicItemInfo {

	@Autowired
	ChassisHome chassisHome;
	
	public BasicBladeItemInfo() {
	}
	
	private String getSlotLabel( Item item ) throws DataAccessException{
		StringBuilder str = new StringBuilder();
		String retval = null;
		
		ItItem blade = (ItItem)item;
		
		if (null == blade.getBladeChassis() || 
				(blade.getSubclassLookup().getLkpValueCode() != SystemLookup.SubClass.BLADE && 
				blade.getSubclassLookup().getLkpValueCode() != SystemLookup.SubClass.BLADE_SERVER) ) {
			return null;
		}

		long facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
		if (null != blade.getFacingLookup()) {
			facingLkpValueCode = blade.getFacingLookup().getLkpValueCode();
		}
		else {
			long bladeRailsUsedLkpValueCode = -1;
			if (null != blade.getMountedRailLookup()) {
				bladeRailsUsedLkpValueCode = blade.getMountedRailLookup().getLkpValueCode();
			}
			/* The facing information for the blade is not updated in the database. 
			 * If updated use the  blade.getFacingLookup().getLkpValueCode() */
			if (SystemLookup.RailsUsed.FRONT == bladeRailsUsedLkpValueCode || 
				SystemLookup.RailsUsed.BOTH == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
			}
			else if (SystemLookup.RailsUsed.REAR == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.REAR;
			}
		}		

		if( blade != null && blade.getModel() != null ){
			ItItem chassisItem = chassisHome.getItItemDomainObject(blade.getBladeChassis().getItemId());
			ModelChassis modelChassis = chassisHome.getBladeChassis(chassisItem.getModel().getModelDetailId(), facingLkpValueCode);
			Map<Long, String> sortedSlotNumbers = chassisHome.getSortedSlotNumber(modelChassis, blade);
			if(sortedSlotNumbers.size() > 0 ){
				for( String value : sortedSlotNumbers.values() ){
					str.append(value);
					str.append(",");
				}
				//take out last comma
				retval = str.toString().substring(0, str.length() - 1 );
			}
		}

		return retval;		
	}
	
	@Override
	public void collectItemInfo( Item item ) {
		super.collectItemInfo(item);
			
		ItItem blade = (ItItem)item;
		if(  blade.getBladeChassis() != null ){
			setCmbChassis(blade.getBladeChassis().getItemName());
		}
		if( item.getFacingLookup() != null ) {
			setRadioChassisFace(item.getFacingLookup().getLkpValue());
		}
		try {
			setCmbSlotPosition(getSlotLabel(item));
		} catch (DataAccessException e) {
			setCmbSlotPosition(null);
			log.error("DataAccessException: " + e.getMessage() + " for item: " + item.getItemName());
		}
	}
	@Override
	protected void setPlacementGroup(Item item){
		setPlacementGroup(PlacementGroup.UNASSIGNED.toString());

		ItItem blade = (ItItem)item;
		LksData facingLookup = blade.getFacingLookup();
		if( blade.getBladeChassis() != null ){
			setPlacementGroup(PlacementGroup.UNSLOTTED.toString());

			if( facingLookup != null && blade.getSlotPosition() > 0){
				if( facingLookup.getLkpValueCode() == SystemLookup.ChassisFace.FRONT ){
					setPlacementGroup(PlacementGroup.FRONT_BLADE.toString());
				}else if( facingLookup.getLkpValueCode() == SystemLookup.ChassisFace.REAR ){
					setPlacementGroup(PlacementGroup.BACK_BLADE.toString());
				}
			}
		}
	}		

}
