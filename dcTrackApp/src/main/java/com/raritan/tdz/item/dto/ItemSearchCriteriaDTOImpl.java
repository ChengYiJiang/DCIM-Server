/**
 * 
 */
package com.raritan.tdz.item.dto;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.raritan.tdz.beanvalidation.annotations.LksValueCode;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class ItemSearchCriteriaDTOImpl implements ItemSearchCriteriaDTO {
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#getFilter()
	 */
	@Override
	public ItemSearchFilterDTO getFilter() {
		return filter;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#setFilter(java.util.Map)
	 */
	@Override
	public void setFilter(ItemSearchFilterDTO filter) {
		this.filter = filter;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#getItemClassLkpValueCode()
	 */
	@Override
	public Long getItemClassLkpValueCode() {
		return itemClassLkpValueCode;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#setItemClassLkpValueCode(java.lang.Long)
	 */
	@Override
	public void setItemClassLkpValueCode(Long itemClassLkpValueCode) {
		this.itemClassLkpValueCode = itemClassLkpValueCode;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#getSortField()
	 */
	@Override
	public String getSortField() {
		return sortField;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#setSortField(java.lang.String)
	 */
	@Override
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#getMaxLinesPerPage()
	 */
	@Override
	public Integer getMaxLinesPerPage() {
		return maxLinesPerPage;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#setMaxLinesPerPage(java.lang.Integer)
	 */
	@Override
	public void setMaxLinesPerPage(Integer maxLinesPerPage) {
		this.maxLinesPerPage = maxLinesPerPage;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#getPageNumber()
	 */
	@Override
	public Integer getPageNumber() {
		return pageNumber;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#setPageNumber(java.lang.Integer)
	 */
	@Override
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#getSortDescending()
	 */
	@Override
	public Boolean getSortDescending() {
		return sortOrder;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchCriteriaDTO#setSortDescending(java.lang.Boolean)
	 */
	@Override
	public void setSortDescending(Boolean sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	@NotNull
	@Valid
	private ItemSearchFilterDTO filter;

	@NotNull
	@LksValueCode(lkpType = SystemLookup.LkpType.CLASS,
					specialLkpValueCodes = { SystemLookup.SpecialClass.ALL_CLASS, SystemLookup.SpecialClass.ALL_IN_CABINET})
	private Long itemClassLkpValueCode;
	
	private String sortField;
	
	private Boolean sortOrder;
	
	private Integer maxLinesPerPage = -1;

	private Integer pageNumber;
}
