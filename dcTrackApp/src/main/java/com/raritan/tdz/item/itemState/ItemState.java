/**
 * 
 */
package com.raritan.tdz.item.itemState;


import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

/**
 * @author prasanna
 *
 */
public interface ItemState  extends Validator{
	/**
	 * Get the editability bean based on the status of item
	 * @param item
	 * @return
	 */
	public RulesNodeEditability getEditability(Item item);
	
	/**
	 * Returns true if the item can transition from its 
	 * current state in database to the new state given 
	 * in the item object.
	 * @param item
	 * @return
	 */
	public boolean canTransition(Item item);
	
	/**
	 * Performs item object changes for the specific state while before persisting
	 * into the database
	 * @param item
	 * @throws ClassNotFoundException 
	 * @throws BusinessValidationException 
	 * @throws DataAccessException 
	 */
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException;
	
	/**
	 * This will give you all the allowable states for the given state of the item
	 * @param statusLkpValueCode
	 * @return
	 */
	public List<Long> getAllowableStates();
	
    /**
     * Returns true if user is permited to transition item from old state
     * to new state. Otherwise, returns false
     * @param item - item in question
     * @param userInfo - info about user
     * @param errors - errors
     * @return
     */
    public boolean isTransitionPermittedForUser(Item item, Long newState, UserInfo userInfo);
    
    
    /**
     * Validate mandatory fields for a given status of item.
     * Note that this not only validates the mandatory fields for that state, it also makes sure
     * that all user defined required fields are also filled up in the item.
     * 
     * This method is used while validating a request for an item.
     * @param item
     * @param errors TODO
     * @param newStatusLkpValueCode TODO
     * @throws ClassNotFoundException 
     * @throws DataAccessException 
     */
    public void validateMandatoryFields(Item item, Errors errors, Long newStatusLkpValueCode) throws DataAccessException, ClassNotFoundException;
    
    /**
     * Validate parent child constraint for a given status of item.
     * Note that this not only validates the mandatory fields for that state, it also makes sure
     * that all user defined required fields are also filled up in the item.
     * 
     * This method is used while validating a request for an item.
     * @param item
     * @param errors TODO
     * @param newStatusLkpValueCode TODO
     * @param errorCodePrefix 
     * @throws ClassNotFoundException 
     * @throws DataAccessException 
     */
    public void validateParentChildConstraint(Item item, Errors errors, Long newStatusLkpValueCode, String errorCodePrefix) throws DataAccessException, ClassNotFoundException;

	public void validateAllButReqFields(Object target, UserInfo userSession, Errors errors);

}
