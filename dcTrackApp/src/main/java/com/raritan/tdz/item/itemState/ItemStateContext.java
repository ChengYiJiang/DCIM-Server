/**
 * 
 */
package com.raritan.tdz.item.itemState;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

/**
 * This represents the item state context
 * which will be the external interface to 
 * ensure that the state of the item is mainted
 * @author prasanna
 *
 */
public interface ItemStateContext extends Validator{
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
	 * @param errors TODO
	 * @return
	 */
	public boolean canTransition(Item item, Errors errors);
	
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
	 * @param item
	 * @param userInfo
	 * @return
	 */
	public List<Long> getAllowableStates(Item item, UserInfo userInfo);
	
    /**
     * Returns true if user is permited to transition item from old state
     * to new state. Otherwise, returns false
     * @param item - item in question
     * @param userInfo - info about user
     * @param errors - errors
     * @return
     */

    public boolean isTransitionPermittedForUser(Item item, UserInfo userInfo, Errors errors);
    

    /**
     * Validate mandatory fields for a given state that the item is going to be transitioned to.
     * @param item
     * @param newStatusLkpValueCode - new status that the item will be transitioned to.
     * @param errors
     * @throws ClassNotFoundException 
     * @throws DataAccessException 
     */
    public void validateMandatoryFields(Item item, Long newStatusLkpValueCode, Errors errors) throws DataAccessException, ClassNotFoundException;
    
    /**
     * Validate Parent Child constraints for a given state that the item is going to be transitioned to.
     * @param item
     * @param newStatusLkpValueCode - new status that the item will be transitioned to.
     * @param errors
     * @throws ClassNotFoundException 
     * @throws DataAccessException 
     */
    public void validateParentChildConstraint(Item item, Long newStatusLkpValueCode, Errors errors) throws DataAccessException, ClassNotFoundException;

    public void validateAllButReqFields(Object target, UserInfo sessionUser, Errors errors);

    public List<Long> getStatusList(ModelDetails model);
}
