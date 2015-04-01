/**
 * 
 */
package com.raritan.tdz.item.dto;

import java.util.List;

/**
 * @author prasanna
 * This will have the list of results from the item search
 * and the counts associated with this search.
 */
public interface ItemSearchResultSetDTO {
	/**
	 * This will return the item search results
	 * @return List of ItemSearchResultDTOs
	 */
	public abstract List<ItemSearchResultDTO> getSearchResults();
	
	/**
	 * This will return the total count of all the items for 
	 * a specific class provided in the ItemSearchCriteriaDTO
	 * @return
	 */
	public abstract int getTotalCount();
}
