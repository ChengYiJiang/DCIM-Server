/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.hibernate.Criteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.springframework.context.MessageSource;

import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 *
 */
public class ItemSearchHelperUPS extends ItemSearchHelperME {
	
	public ItemSearchHelperUPS(MessageSource messageSource,
			DozerBeanMapper dzMapper, Map<String, ItemSearchTypeRestriction> itemSearchTypeRestriction,
			DtoToDomainObjectTrace commonItemSearchDtoToDomainTrace) {
		super(messageSource, dzMapper, itemSearchTypeRestriction, commonItemSearchDtoToDomainTrace);
	}

	
	public DtoToDomainObjectTrace getMeItemSearchResultDTOToDomainTrace() {
		return meItemSearchResultDTOToDomainTrace;
	}


	public void setMeItemSearchResultDTOToDomainTrace(
			DtoToDomainObjectTrace meItemSearchResultDTOToDomainTrace) {
		this.meItemSearchResultDTOToDomainTrace = meItemSearchResultDTOToDomainTrace;
	}

	@Override
	protected ProjectionList getProjections() {
		return super.getProjections();	
	}

	@Override
	protected void addAlias(Criteria searchCriteria) {
		super.addAlias(searchCriteria);
	}

	@Override
	protected void addSearchResult(ItemSearchResultDTOImpl searchResult, HashMap item) {
		searchResult.setRatingV((Double)item.get("_ratingV"));
		searchResult.setRatingKW((Double)item.get("_ratingKW"));
		searchResult.setRatingKva((Double)item.get("_ratingKva"));
		searchResult.setPowerFactor(searchResult.getRatingKW()/searchResult.getRatingKva());
	}

	@Override
	protected void applyFilters(Criteria criteria, String filterKey, String filterValue) {
		// TODO Auto-generated method stub
		
	}
}
