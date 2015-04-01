package com.raritan.tdz.searchplace.dao;


import com.raritan.tdz.searchplace.domain.PlaceSearch;
import com.raritan.tdz.dao.Dao;

/**
 * This will be a DAO class for PlaceSearch object
 * @author Santo Rosario
 */
	
public interface PlaceSearchDAO extends Dao<PlaceSearch>{
	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return PlaceSearch
	 */	
	public PlaceSearch loadPlaceSearch(Long id); 

	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return PlaceSearch
	 */	
	public PlaceSearch loadPlaceSearch(Long id, boolean readOnly); 
	
	/**
	 * Load/Read an item from the database using current hiberante session
	 * 
	 * @param id - place search id
	 * @return PlaceSearch
	 */
	public PlaceSearch getPlaceSearch(Long id);	

}

