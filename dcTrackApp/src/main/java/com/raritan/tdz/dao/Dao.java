package com.raritan.tdz.dao;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

/**
 * The basic GenericDao interface with CRUD methods
 */

public interface Dao<T extends Serializable>
{

	/**
	 * Create a new record in database
	 * @param Object to be created (item, model, port, etc)
	 * @return ID of created object
	 */	
    Long create(T newInstance);

	/**
	 * Get/Read a record from database using current session
	 * @param ID of record to be read
	 * @return Object record (item, model, port, etc)
	 */	   
    T read(Long id);

	/**
	 * Update an existing record in database
	 * @param Object to be updated (item, model, port, etc)
	 * @return none
	 */	    
    void update(T transientObject);

	/**
	 * Update an existing record in database
	 * @param Object to be updated (item, model, port, etc)
	 * @return none
	 */	    
    void update(List<T> transientObject);
    
    /**
     * Update an existing record with merge API
     * @param transientObject
     */
    T merge(T transientObject);

    /**
     * Update an existing record with merge API
     * @param transientObject
     */
    void merge(List<T> transientObject);

    
	/**
	 * Delete an existing record from database
	 * @param Object to be updated (item, model, port, etc)
	 * @return none
	 */	       
    void delete(T persistentObject);
    
	/**
	 * Get current hibernate session
	 * @param none
	 * @return session
	 */	        
    Session getSession();
    
	/**
	 * Open a new hibernate session
	 * @param none
	 * @return new session
	 */	        
    Session getNewSession();
    
    /**
	 * Close session open with by calling the getNewSession() method
	 * @param newSession - session to be closed
	 * @return none
	 */	  
    void closeNewSession(Session newSession);
    
    
    /**
     * This is required when we cannot directly cast a entity
     * as hibernate provides a proxy object
     * @param entity
     * @return
     */
    public <T> T initializeAndUnproxy(T entity);

    /**
     * Update an existing record with merge API. This will not run the flush.
     * @param transientObject
     */
	void mergeOnly(T transientObject);

    /**
     * Update an existing set of records with merge API. This will not run the flush.
     * @param transientObject
     */
	void mergeOnly(Set<T> transientObjects);

	/**
	 * get the map of the field and the corresponding value object
	 * @param clazz
	 * @param idField
	 * @param id
	 * @param fields
	 * @return
	 */
	Map<String, Object> getFieldsValue(Class<?> clazz, String idField,
			Object id, List<String> fields);
       
}
