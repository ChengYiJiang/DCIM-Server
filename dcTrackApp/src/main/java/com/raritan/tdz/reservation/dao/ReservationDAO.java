package com.raritan.tdz.reservation.dao;


import com.raritan.tdz.domain.Reservations;
import com.raritan.tdz.dao.Dao;

/**
 * This will be a DAO class for Reservations object
 * @author Santo Rosario
 */
	
public interface ReservationDAO extends Dao<Reservations>{
	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return Reservations
	 */	
	public Reservations loadReservation(Long id); 

	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return Reservations
	 */	
	public Reservations loadReservation(Long id, boolean readOnly); 
	
	/**
	 * Load/Read an item from the database using current hiberante session
	 * 
	 * @param id - place search id
	 * @return Reservations
	 */
	public Reservations getReservation(Long id);	

	/**
	 * Delete an item from the database using current hiberante session
	 * 
	 * @param id - reservation id
	 * @return none
	 */
	public void delete(Long id);	

}

