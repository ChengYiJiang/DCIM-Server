package com.raritan.tdz.searchplace.dao;


import com.raritan.tdz.searchplace.domain.PlaceSearchPortPower;
import com.raritan.tdz.dao.Dao;

/**
 * This will be a DAO class for PlaceSearchPortData object
 * @author Santo Rosario
 */
	
public interface PlaceSearchPortPowerDAO extends Dao<PlaceSearchPortPower>{
	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return PlaceSearchPortPower
	 */	
	public PlaceSearchPortPower loadPlaceSearchPortPower(Long id); 

	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return PlaceSearchPortPower
	 */	
	public PlaceSearchPortPower loadPlaceSearchPortPower(Long id, boolean readOnly); 
	
	/**
	 * Load/Read an item from the database using current hiberante session
	 * 
	 * @param id - place search id
	 * @return PlaceSearchPortPower
	 */
	public PlaceSearchPortPower getPlaceSearchPortPower(Long id);	

}

