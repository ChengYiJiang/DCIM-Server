package com.raritan.tdz.location.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.DataCenterLocationDetails;

/**
 * This will be a DAO class for Location object
 * @author prasanna
 */
	
public interface LocationDAO extends Dao<DataCenterLocationDetails>{
	/**
	 * Load/Read an existing location from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of location to be loaded
	 * @return DataCenterLocationDetails
	 */	
	public DataCenterLocationDetails loadLocation(Long id); 

	/**
	 * Load/Read an existing location from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of location to be loaded
	 * @return DataCenterLocationDetails
	 */	
	public DataCenterLocationDetails loadLocation(Long id, boolean readOnly); 
	
	/**
	 * Load/Read an item from the database using current hiberante session
	 * 
	 * @param id - location id
	 * @return DataCenterLocationDetails
	 */
	public DataCenterLocationDetails getLocation(Long id);
	
	/**
	 * get PIQ host by location id.
	 * @param locationId
	 * @return String piq host
	 */
	public String getPiqHostByLocationId(long locationId);
	
	/**
	 * get PIQ integration setting by location id.
	 * @param locationId
	 * @param lkpValueCode
	 * @return String pic integration setting 
	 */
	public String getPiqSettingByLocationId(long locationId,long lkpValueCode);
	
	public Long createLocation(DataCenterLocationDetails location);
	
	public List<Long> getLocationIdByPIQHost(String piqHost);

	/**
	 * get the lcoation code for a given location id
	 * @param locationId
	 * @return
	 */
	public String getLocationCode(Long locationId);
	
	/**
	 * Get the location Id based on location name
	 * @param locationCode
	 * @return
	 */
	public Long getLocationIdByCode(String locationCode);
	
	/**
	 * delete the location and all associated reference for this location
	 */
	public void deleteLocationAndItems(Long locationId, String userName);

	/**
	 * clear all piq assciations in the given location
	 * @param locationId
	 */
	public void unmapLocationWithPIQ(Long locationId);

}

