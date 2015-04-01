/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 * This is the Item search (IS) Helper interface. This is used by ItemSearchImpl for
 * creating a Hibernate query based on various class combinations and also 
 * getting the appropriate DTO data to be filled and sent back to client
 */
public interface ItemSearchHelper {
	
	/**
	 * create a Hibernate Criteria Object given the itemCriteria DTO 
	 * @param itemCriteria This is the itemSearchCriteria DTO sent by the client
	 * @param trace This is the trace object that contains the trace with which we can find the search key.
	 * @param session Hibernate Session
	 * @return Hibernate Criteria Object
	 * @throws DataAccessException
	 * @throws BusinessValidationException
	 * @throws SystemException 
	 */
	Criteria createItemSearchCriteria(ItemSearchCriteriaDTO itemCriteria,
			DtoToDomainObjectTrace trace, Session session) throws BusinessValidationException, SystemException;
	
	/**
	 * Based on the hibernate query results, this method will convert the list into the appropriate 
	 * ItemSearchResultDTO to be sent to client.
	 * @param queryResults Hibernate query result list.
	 * @return A list of ItemSearchResultDTO. Please note that depending on the Item class some of the fields may be empty.
	 */
	List<ItemSearchResultDTO> getItemSearchResultDTO(List queryResults);
	
	/**
	 * Gets the total item count for a given class lkp value code
	 * @param classLkpValueCode
	 * @param session TODO
	 * @return
	 */
	Long getTotalItemCountForClass(Long classLkpValueCode, Session session);
}
