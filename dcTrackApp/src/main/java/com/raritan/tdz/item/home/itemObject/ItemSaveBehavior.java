/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * This interface will help the main ItemObjectTemplate to 
 * perform specific business logic before/after saving the item
 * @author prasanna
 *
 */
public interface ItemSaveBehavior {
	
	/**
	 * Performs any pre-validation updates to the item
	 * @param item
	 * @param additionalArgs
	 * @throws BusinessValidationException
	 */
	public void preValidateUpdate(Item item, Object...additionalArgs) throws BusinessValidationException;
	
	/**
	 * Performs any pre-save operations
	 * @param item
	 * @param additionalArgs -a variable additional arguments for processing
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 */
	public void preSave(Item item, Object... additionalArgs) throws BusinessValidationException, DataAccessException;
	
	/**
	 * Performs any post-save operations
	 * @param item
	 * @param sessionUser TODO
	 * @param additionalArgs TODO
	 * @throws BusinessValidationException
	 * @throws DataAccessException TODO
	 */
	public void postSave(Item item, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException, DataAccessException;
	
	/**
	 * Will tell the caller if the implementation supports the domain object
	 * @param domainObjectNames
	 * @return
	 */
	public boolean canSupportDomain(String... domainObjectNames);
}
