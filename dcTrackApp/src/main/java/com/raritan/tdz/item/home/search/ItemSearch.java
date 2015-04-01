/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.util.List;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.util.ObjectTracer;

/**
 * @author prasanna
 * This is an interface to perform item search
 */
public interface ItemSearch {
	
	/**
	 * This is used for searching an item based on the itemCriteria
	 * @param itemCriteria - DTO that has the criteria for item search
	 * @return List of itemSearchResultDTO
	 * @throws BusinessValidationException TODO
	 * @throws SystemException 
	 * @throws Throws a DataAccessException that may be a business validation exception
	 */
	public List<ItemSearchResultDTO> search(ItemSearchCriteriaDTO itemCriteria) throws BusinessValidationException, SystemException;
	
	/**
	 * Gets the total item count for the given classLkpValueCode 
	 * @param classLkpValueCode
	 * @return
	 */
	public Long getTotalItemCountForClass(Long classLkpValueCode);

}
