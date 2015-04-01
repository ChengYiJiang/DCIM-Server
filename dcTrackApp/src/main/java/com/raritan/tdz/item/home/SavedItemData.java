package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;

/**
 * This class is used for obtaining the item state key data which is used to determine if a transition is possible.
 * It provides static methods for capturing current item state as a Thread local variable so that this
 * data doesn't have to be passed as arguments in so many places of the item save code.
 * 
 * FIXME: We are copying lots of information from the original item object. Can we just make a safe
 * clone of the item domain object internally and leave the public accessors as they are?
 * 
 * @author Andrew Cohen
 */
public class SavedItemData {
	
	// Item Class is part of the state key
	private Long itemClassValueCode;
	
	// Item Status is part of the state key
	private Long itemStatusValueCode;
	
	private LkuData itemAdminTeamLookup;
	
	private Users itemAdminUser;
	
	// Current item model - we need to do some placement validation if the model changes since (RU Height might change)
	private ModelDetails itemModel;
	
	// The original item data
	private Item origItem;
	
	private Collection<Long> availablePositions;
	
	private Map<Long, DataPort> dataPorts = new HashMap<Long, DataPort>();
	
	private Map<Long, PowerPort> powerPorts = new HashMap<Long, PowerPort>();
	
	private Map<Long, SensorPort> sensorPorts = new HashMap<Long, SensorPort>();
	
	// Store item state key as a thread local so we don't have to pass it aroundS
	private static ThreadLocal<SavedItemData> savedItemData = new ThreadLocal<SavedItemData>();
	
	/**
	 * Capture the item state key for the specified item.
	 * This is stored in a thread local variable.
	 * @param item the item
	 */
	public static void captureItemData(Item item, ItemPlacementHome placementHome) throws DataAccessException {
		if (item == null) {
			clearCurrentItemSaveDataKey();
			return;
		}
		
		SavedItemData key = new SavedItemData();
		
		if (item.getStatusLookup() != null && item.getClassLookup() != null) {
			key.itemClassValueCode = item.getClassLookup().getLkpValueCode();
			key.itemStatusValueCode = item.getStatusLookup().getLkpValueCode();
		}
		if (item.getItemServiceDetails() != null) {
			if(item.getItemServiceDetails().getItemAdminTeamLookup() != null){
			//	FIXME: Do I need to allocate new object here or reference is ok?
				key.itemAdminTeamLookup = item.getItemServiceDetails().getItemAdminTeamLookup();			
			}
			if(item.getItemServiceDetails().getItemAdminUser() != null){
			//	FIXME: Do I need to allocate new object here or reference is ok?
				key.itemAdminUser = item.getItemServiceDetails().getItemAdminUser();				
			}
		}
		if (null != item.getDataPorts()) 
			for (DataPort port: item.getDataPorts()) 
				key.dataPorts.put(port.getPortId(), port);

		if (null != item.getPowerPorts()) 
			for (PowerPort port: item.getPowerPorts()) 
				key.powerPorts.put(port.getPortId(), port);

		if (null != item.getSensorPorts()) 
			for (SensorPort port: item.getSensorPorts()) 
				key.sensorPorts.put(port.getPortId(), port);

		key.itemModel = item.getModel();
		//key.origItem = (Item)item.clone(); //SANTO ?????????
		key.origItem = item;
		
		savedItemData.set( key );
		
		if(key.itemModel != null && placementHome != null){
			key.availablePositions = placementHome.getAvailablePositions( item, null );
		}
		else{
			key.availablePositions = new ArrayList<Long>();
		}
	}
	
	/**
	 * Get the current item state key associated with the current thread
	 * @return
	 */
	public static String getCurrentItemStateKey() {
		SavedItemData key = savedItemData.get();
		if( key != null ){
			return getItemStateKey(key.getItemClassValueCode(), key.getItemStatusValueCode());
		}else return null;
		
	}
	
	public Collection<Long> getAvailablePositions() {
		return availablePositions;
	}

	void setAvailablePositions(Collection<Long> availablePositions) {
		this.availablePositions = availablePositions;
	}

	/**
	 * Get the current original item data.
	 * @return
	 */
	public static SavedItemData getCurrentItem() {
		return savedItemData.get();
	}
	
	/**
	 * Clears the item state key associated with the current thread
	 */
	public static void clearCurrentItemSaveDataKey() {
		if( savedItemData != null) savedItemData.set( null );
	}
	
	/**
	 * Get the item state key for a particular item
	 * @param item
	 * @return
	 */
	public static String getItemStateKey(Item item) {
		if (item == null) return "";
		if (item.getStatusLookup() != null && item.getClassLookup() != null){
			return getItemStateKey(item.getClassLookup().getLkpValueCode(), item.getStatusLookup().getLkpValueCode());
		}
		return "";
	}
	/**
	 * Get the item status value code.
	 * @return a Long
	 */
	public static Long getCurrentItemStatusValueCode() {
		SavedItemData key = savedItemData.get();
		if( key != null ) return key.getItemStatusValueCode();
		return null;
	}

	/**
	 * Get the current admin team lookup
	 * @return LkuData
	 */
	public static LkuData getCurrentItemAdminTeamLookup(){
		SavedItemData key = savedItemData.get();
		if( key != null) return key.getItemAdminTeamLookup();
		else return null;
	}
	
	/**
	 * get the current admin user
	 * @return Users
	 */
	public static Users getCurrentItemAdminUser(){
		SavedItemData key = savedItemData.get();
		if( key != null ) return key.getItemAdminUser();
		else return null;
	}

	/**
	 * Get the item class value code.
	 * @return a Long
	 */
	public Long getItemClassValueCode() {
		return itemClassValueCode;
	}

	/**
	 * Get the item status calue code
	 * @return Long
	 */
	public Long getItemStatusValueCode(){
		return itemStatusValueCode;
	}
	/**
	 * Get the value of saved Item's Team lookup
	 * @return
	 */
	public LkuData getItemAdminTeamLookup() {
		return itemAdminTeamLookup;
	}

	/**
	 * Get the value of Item's admin user
	 * @return
	 */
	public Users getItemAdminUser() {
		return itemAdminUser;
	}
	
	/**
	 * Get the original model of the item.
	 * If the model changes, then we need to do some placement validation since the RU Height might change.
	 * @return
	 */
	public ModelDetails getItemModel() {
		return itemModel;
	}
	
	/**
	 * Get the current item data.
	 * @return
	 */
	public Item getSavedItem() {
		return origItem;
	}
	
	
	private static String getItemStateKey(Long classLookupValue, Long statusLookupValue) {
		StringBuffer lookupValue = new StringBuffer();
		
		if (classLookupValue != null && statusLookupValue != null) {
			lookupValue.append( classLookupValue.toString() );
			lookupValue.append(":");
			lookupValue.append( statusLookupValue ); 
		}
		return lookupValue.toString();
	}

	public Map<Long, DataPort> getDataPorts() {
		return dataPorts;
	}

	public Map<Long, PowerPort> getPowerPorts() {
		return powerPorts;
	}

	public Map<Long, SensorPort> getSensorPorts() {
		return sensorPorts;
	}

	public static Map<Long, DataPort> getCurrentDataPorts() {
		SavedItemData key = savedItemData.get();
		if( key != null ) return key.getDataPorts();
		return null;
	}

	public static Map<Long, PowerPort> getCurrentPowerPorts() {
		SavedItemData key = savedItemData.get();
		if( key != null ) return key.getPowerPorts();
		return null;
	}

	public static Map<Long, SensorPort> getCurrentSensorPorts() {
		SavedItemData key = savedItemData.get();
		if( key != null ) return key.getSensorPorts();
		return null;

	}

}
