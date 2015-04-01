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
import org.hibernate.criterion.Restrictions;
import org.springframework.context.MessageSource;

import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 *
 */
public class ItemSearchHelperFloorPDU extends ItemSearchHelperME {

	public ItemSearchHelperFloorPDU(MessageSource messageSource,
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
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.alias(Projections.property("lineVolts"),"_lineVolts"));
		proList.add(super.getProjections());
		return proList;
	}

	@Override
	protected void addAlias(Criteria searchCriteria) {
		super.addAlias(searchCriteria);
	}

	@Override
	protected void addSearchResult(ItemSearchResultDTOImpl searchResult, HashMap item) {
		searchResult.setRatingV((Double)item.get("_lineVolts"));
		searchResult.setRatingAmps((Double)item.get("_ratingAmps"));
		searchResult.setRatingKva((Double)item.get("_ratingKva"));
		searchResult.setNumPorts((Integer)item.get("_numPorts"));
	}
	
	@Override
	protected void applyFilters(Criteria criteria, String filterKey, String filterValue) {
		criteria.add(Restrictions.isNull("parentItem"));
	}

}
