package com.raritan.tdz.item.home;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A thin business wrapper for an item domain object that is
 * used to define "behaviors" for particular types of items.
 * @author Andrew Cohen
 */
public interface ItemObject extends Validator {

	/**
	 * @return the item subclass lookup codes associated with this concrete ItemObject. 
	 */
	public Set<Long> getSubclassLookupValueCodes();
	
	/**
	 * Initializes the item object
	 * @param item
	 */
	public void init(Item item);
	
	/**
	 * saveItem - saves the item based on itemObject.
	 * @param itemId
	 * @param dtoList
	 * @param sessionUser
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	public Map<String, UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> dtoList, UserInfo sessionUser)
			throws ClassNotFoundException, BusinessValidationException, Throwable;
	
	/**
	 * getItemDetails will return a client readable data for a specific item.
	 * @param itemId
	 * @param unit
	 * @return
	 * @throws Throwable
	 */
	public Map<String, UiComponentDTO> getItemDetails(Long itemId, String unit)
			throws Throwable;
	
	/**
	 * deleteItem - delete the item based on itemObject.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	public boolean deleteItem() throws Throwable;

	/**
	 * Get the itemName from the current itemObject.
	 * @return
	 */
	public String getItemName();
	
	/**
	 * Capture the item data before saving/updating it into the database
	 * @param item
	 * @param placementHome
	 * @param itemId TODO
	 * @throws DataAccessException 
	 */
	public void captureItemData(Item item, ItemPlacementHome placementHome, Long itemId) throws DataAccessException;
	
	/**
	 * Clear the captured itemData
	 */
	
	public void clearCapturedItemData(Long itemId);
	
	public boolean isItemPlacementValid(Item item, Errors errors) throws DataAccessException;
	
	public boolean isLicenseRequired();
}
