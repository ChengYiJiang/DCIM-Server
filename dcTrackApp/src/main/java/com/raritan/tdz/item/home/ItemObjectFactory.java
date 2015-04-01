package com.raritan.tdz.item.home;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.home.itemObject.ItemObjectTemplate;
import com.raritan.tdz.item.home.ItemObjectPassiveItem;

/**
 * A factory for obtaining an item business object for a specified item domain object.
 * @author Andrew Cohen
 */
public interface ItemObjectFactory {

	/**
	 * Registers an ItemObject class for each item subclass lookup value code.
	 * @param itemObjectBeans
	 */
	public void setItemClasses( Map<String, String> itemObjectBeans);
	
	
	/**
	 * Creates an ItemObject business wrapper for the specified item.
	 * @param itemId
	 * @return
	 */
	public ItemObject getItemObject(long itemId);
	
	/**
	 * Creates an ItemObject business wrapper for the specified item domain object.
	 * 
	 * @param item
	 * @return
	 */
	public ItemObject getItemObject(Item item);
	
	/**
	 * Creates an ItemObjectTemplate for a specified modelId
	 * @param modelId
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ItemObjectTemplate getItemObjectFromModelId(Long modelId) throws InstantiationException, IllegalAccessException;
	
	/**
	 * Creates an ItemObjectTemplate for a specified itemId
	 * @param itemId
	 * @return
	 */
	public ItemObjectTemplate getItemObjectFromItemId(Long itemId);
	
	/**
	 * Creates an ItemObjectTemplate for specified valueIdDTOList
	 * @param itemId TODO
	 * @param valueIdDTOList
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ItemObjectTemplate getItemObject(Long itemId, List<ValueIdDTO> valueIdDTOList) throws InstantiationException, IllegalAccessException;


	public ItemObjectTemplate getItemObjectForVM();
	
	//public ItemObject getItemObjectUsingMounting(long itemId);
	//public ItemObject getItemObjectUsingMounting(Item item);
}
