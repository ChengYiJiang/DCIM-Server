package com.raritan.tdz.searchplace.dao;


import com.raritan.tdz.searchplace.domain.PlaceSearchPortData;
import com.raritan.tdz.dao.Dao;

/**
 * This will be a DAO class for PlaceSearchPortData object
 * @author Santo Rosario
 */
	
public interface PlaceSearchPortDataDAO extends Dao<PlaceSearchPortData>{
	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return PlaceSearchPortData
	 */	
	public PlaceSearchPortData loadPlaceSearchPortData(Long id); 

	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return PlaceSearchPortData
	 */	
	public PlaceSearchPortData loadPlaceSearchPortData(Long id, boolean readOnly); 
	
	/**
	 * Load/Read an item from the database using current hiberante session
	 * 
	 * @param id - place search id
	 * @return PlaceSearchPortData
	 */
	public PlaceSearchPortData getPlaceSearchPortData(Long id);	

}

