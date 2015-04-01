/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;

/**
 * This helps the itemObject to perform any split or join of
 * valueIdDTOs
 * @author prasanna
 *
 */
public interface ItemSaveDTOHelper {
	/**
	 * Split the valueIdDTO list. The result will be a map of domainObjectName to its list of valueIdDTO
	 * @param itemId TODO
	 * @param valueIdDTOList
	 * @param sessionUser TODO
	 * @return
	 * @throws Throwable 
	 */
	public Map<String, List<ValueIdDTO>> splitValueIdDTO(Long itemId, List<ValueIdDTO> valueIdDTOList, UserInfo sessionUser) throws Throwable;
	
	/**
	 * Sets the results into an internal structure (left to the implementation on the structure itself)
	 * @param domainEntityClass
	 * @param resultMap
	 */
	public void setSaveItemResult(String domainEntityClass, Map<String,UiComponentDTO> resultMap);
	
	/**
	 * Process anything that needs to be done prior to save operation
	 * @param domainEntityClass - This could be ItItem.class for example.
	 * @param valueIdDTOList - Generally the valueIdDTOList will be the one that is already split.
	 * @throws Throwable
	 */
	public void preSave(String domainEntityClass, List<ValueIdDTO> valueIdDTOList) throws Throwable;
	
	/**
	 * Gets the itemId from an internal structure given the type of the domain object.
	 * @param domainEntityClass
	 * @return
	 */
	public Long getItemId(String domainEntityClass);
	
	/**
	 * This sets the itemId to the internal structure so that it can be retrived 
	 * via the getItemId using domainEntityClass name.
	 * @param itemId
	 */
	public void setItemId(Long itemId);
	
	/**
	 * Merge the results using the map of domainObjectName to Map of uiId,UiComponentDTO.
	 * Result will be a merged map of uiId to its UiComponentDTO. This will be using
	 * the saved result map to merge them and return the merged map result.
	 * @return
	 */
	public Map<String,UiComponentDTO> mergeResults();
	
	/**
	 * Perform any pre-init before calling splitValueIdDTO.
	 * @param itemId TODO
	 * @param valueIdDTOList
	 * @return
	 * @throws Throwable TODO
	 */
	public Object preInit(Long itemId, List<ValueIdDTO> valueIdDTOList) throws Throwable;
}
