/**
 * 
 */
package com.raritan.tdz.item.dto;

import java.util.List;

/**
 * @author prasanna
 *
 */
public class ItemSearchResultSetDTOImpl implements ItemSearchResultSetDTO {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultSetDTO#getSearchResults()
	 */
	@Override
	public List<ItemSearchResultDTO> getSearchResults() {
		return searchResults;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultSetDTO#getTotalCount()
	 */
	@Override
	public int getTotalCount() {
		return totalCount;
	}


	/**
	 * @param searchResults the searchResults to set
	 */
	public void setSearchResults(List<ItemSearchResultDTO> searchResults) {
		this.searchResults = searchResults;
	}

	/**
	 * @param totalCount the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	private List<ItemSearchResultDTO> searchResults;
	private int totalCount;
}
