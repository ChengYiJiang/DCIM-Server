/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.MessageSource;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 *
 */
public class ItemSearchHelperBase extends ItemSearchHelperTemplate {
	
	private DtoToDomainObjectTrace trace;

	public ItemSearchHelperBase(MessageSource messageSource,
			DozerBeanMapper dzMapper, Map<String, ItemSearchTypeRestriction> itemSearchTypeRestriction,
			DtoToDomainObjectTrace commonItemSearchDtoToDomainTrace) {
		super(messageSource, dzMapper, itemSearchTypeRestriction, commonItemSearchDtoToDomainTrace);
	}
	

	@Override
	protected Criteria createCriteria(Session session) {
		return session.createCriteria(Item.class);
	}


	@Override
	protected void addItemClassRestriction(Criteria searchCriteria, Long classLkpValueCode) {
		searchCriteria.add(Restrictions.eq("classLookup.lkpValueCode",classLkpValueCode.longValue()));
	}


	@Override
	protected ProjectionList getProjections() {
		ProjectionList proList = Projections.projectionList();
	//	proList.add(Projections.alias(Projections.property("subclassLookup.lkpValueCode"),"subclassLkpValueCode"));
		return proList;
	}


	@Override
	protected void addAlias(Criteria searchCriteria) {
	//	searchCriteria.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);
	}


	@Override
	protected void addSearchResult(ItemSearchResultDTOImpl searchResult, HashMap item) {
		Long subClassLookup = (Long) item.get("subclassLkpValueCode");
		if (subClassLookup != null && (subClassLookup.equals(SystemLookup.SubClass.BLADE) || subClassLookup.equals(SystemLookup.SubClass.BLADE_SERVER))){
			searchResult.setCabinetName(null);
			searchResult.setSlotPosition((Long)item.get("_slotPosition"));
			searchResult.setUPosition(new Long(0));
		} else {
			searchResult.setChassisName(null);
			searchResult.setSlotPosition(new Long(0));
			searchResult.setUPosition((Long)item.get("_uPosition"));
		}
	}


	@Override
	protected void applyFilters(Criteria criteria, String filterKey, String filterValue) {
		//This is to avoid "DEVICE PORT TEMPLATE ITEM" record from appearing
		//I am assuming that this is the correct filter to use!
		criteria.add(Restrictions.isNotNull("model"));	
	}


	public DtoToDomainObjectTrace getTrace() {
		return super.getTrace();
	}


	public void setTrace(DtoToDomainObjectTrace trace) {
		super.setTrace(trace);
	}
	
	

}
