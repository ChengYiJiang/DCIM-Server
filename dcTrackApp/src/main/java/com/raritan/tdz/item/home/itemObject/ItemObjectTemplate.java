/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.validators.ItemValidatorNew;

/**
 * This interface will be used by the itemHome interface to perform
 * various business logic functions related to an item. This will be using other interfaces
 * to achieve the goal and on its own acts more like a template. 
 * @author prasanna
 *
 */
public interface ItemObjectTemplate {
	/**
	 * Saves the given item domain object
	 * @param itemDomain
	 * @param unit
	 * @return
	 * @throws BusinessValidationException
	 */
	public Long saveItem(Object itemDomain, String unit) throws BusinessValidationException;
	
	/**
	 * Gets the item details as a name/value pair. Name being the uiId and value being instance
	 * of uiCompoentDTO.
	 * @param itemId
	 * @param unit
	 * @return
	 * @throws BusinessValidationException
	 * @throws Throwable 
	 */
	public Map<String, UiComponentDTO> getItemDetails(Long itemId, String unit) throws BusinessValidationException, Throwable;
	
	/**
	 * Deletes an item given its id. Note that it can delete only items in new state.
	 * @param itemId
	 * @param validate
	 * @throws BusinessValidationException
	 * @throws Throwable 
	 */
	public void deleteItem(Long itemId, boolean validate, UserInfo userInfo) throws BusinessValidationException, Throwable;
	
	/**
	 * validate if the item can be deleted
	 * @param itemId
	 * @param validate
	 * @param userInfo
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	public void validateDeleteItem(Long itemId, boolean validate, UserInfo userInfo) throws BusinessValidationException, Throwable;
	
	/**
	 * Save an item given the valueIDDTOList
	 * @param itemId
	 * @param valueIdDTOList
	 * @param userInfo
	 * @return
	 * @throws BusinessValidationException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws DataAccessException 
	 * @throws IllegalArgumentException 
	 * @throws Throwable 
	 */
	public Map<String,UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> valueIdDTOList, UserInfo userInfo) throws BusinessValidationException, Throwable;
	
	/**
	 * Convenient setter method for itemValidator
	 * @param itemValidator
	 */
	public void setItemValidator(ItemValidatorNew itemValidator);
	
	/**
	 * Convenient getter method for itemValidator
	 * @return
	 */
	public ItemValidatorNew getItemValidator();
	
	/**
	 * Convenient setter method for itemSaveBehavior
	 * @param itemValidator
	 */
	public void setItemSaveBehaviors(List<ItemSaveBehavior> itemSaveBehaviors);
	
	/**
	 * Convenient getter method for itemSaveBehavior
	 * @return
	 */
	public List<ItemSaveBehavior> getItemSaveBehaviors();
}
