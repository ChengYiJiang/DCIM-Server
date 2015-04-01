package com.raritan.tdz.item.dto;

import java.util.Map;

/**
 * @author prasanna
 * This represents the search criteria DTO
 */
public interface ItemSearchCriteriaDTO {

	/**
	 * @return the filter - This gives you the search key/value pairs
	 */
	public abstract ItemSearchFilterDTO getFilter();

	/**
	 * @param filter Filter that contains search key/value pairs.
	 */
	public abstract void setFilter(ItemSearchFilterDTO filter);

	/**
	 * Please note that for "All In-Cabinet" we will use lkpValueCode = 
	 * If itemClassLkpValueCode == -1 then all the item classes are 
	 * selected for the search
	 * @return the itemClassLkpValueCode - This will be an Item Class Value Code
	 */
	public abstract Long getItemClassLkpValueCode();

	/**
	 * Please note that for "All In-Cabinet" we will use lkpValueCode = ItemSearchCriteriaDTO.allInCabinet
	 * If itemClassLkpValueCode == -1 then all the item classes are 
	 * selected for the search
	 * @param itemClassLkpValueCode the itemClassLkpValueCode to set
	 */
	public abstract void setItemClassLkpValueCode(Long itemClassLkpValueCode);

	/**
	 * @return sort column that user selects
	 */
	public abstract String getSortField();

	/**
	 * If this field is null, then no sorting 
	 * will be performed.
	 * @param sort column that user selects
	 */
	public abstract void setSortField(String sortField);

	/**
	 * @return the sort order enumeration
	 */
	public abstract Boolean getSortDescending();

	/**
	 * If sortField is null then this will be ignored.
	 * @param sort order enumeration
	 */
	public abstract void setSortDescending(Boolean sortOrder);

	/**
	 * @return the maxLinesPerPage
	 */
	public abstract Integer getMaxLinesPerPage();

	/**
	 * If this is -1 all data will be sent ignoring page limit.
	 * @param maxLinesPerPage This is the number of lines that is displayed to the user per page.
	 */
	public abstract void setMaxLinesPerPage(Integer maxLinesPerPage);

	/**
	 * @return the pageNumber
	 */
	public abstract Integer getPageNumber();
	

	/**
	 * This is the current page number the user has selected.
	 * If maxLinesPerPage is -1 then this will be ignored.
	 * @param pageNumber the pageNumber to set
	 */
	public abstract void setPageNumber(Integer pageNumber);
	
}