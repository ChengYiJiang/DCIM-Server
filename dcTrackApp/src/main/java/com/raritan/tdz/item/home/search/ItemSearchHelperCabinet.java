/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.Field;
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

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 *
 */
public class ItemSearchHelperCabinet extends ItemSearchHelperTemplate {

	private DtoToDomainObjectTrace cabinetItemSearchDtoToDomainTrace;
	
	public ItemSearchHelperCabinet(MessageSource messageSource,
			DozerBeanMapper dzMapper, Map<String, ItemSearchTypeRestriction> itemSearchTypeRestriction,
			DtoToDomainObjectTrace commonItemSearchDtoToDomainTrace) {
		super(messageSource, dzMapper, itemSearchTypeRestriction, commonItemSearchDtoToDomainTrace);

	}

	
	public DtoToDomainObjectTrace getCabinetItemSearchDtoToDomainTrace() {
		return cabinetItemSearchDtoToDomainTrace;
	}


	public void setCabinetItemSearchDtoToDomainTrace(
			DtoToDomainObjectTrace cabinetItemSearchDtoToDomainTrace) {
		this.cabinetItemSearchDtoToDomainTrace = cabinetItemSearchDtoToDomainTrace;
	}


	@Override
	protected Criteria createCriteria(Session session) {
		return session.createCriteria(CabinetItem.class);
	}

	@Override
	protected void addItemClassRestriction(Criteria searchCriteria,
			Long classLkpValueCode) {
		searchCriteria.add(Restrictions.eq("classLookup.lkpValueCode",classLkpValueCode.longValue()));
	}

	@Override
	protected ProjectionList getProjections() {
		return super.getProjections(cabinetItemSearchDtoToDomainTrace);
	}

	@Override
	protected void addAlias(Criteria searchCriteria) {
		super.addAlias(cabinetItemSearchDtoToDomainTrace, searchCriteria);
	}

	@Override
	protected void addSearchResult(ItemSearchResultDTOImpl searchResult, HashMap item) {
		searchResult.setGridLocation((String)item.get("_gridLocation"));
		searchResult.setRowLabel((String)item.get("_rowLabel"));
		searchResult.setPositionInRow((Integer)item.get("_positionInRow"));
	}

	@Override
	protected void applyFilters(Criteria criteria, String filterKey, String filterValue) {
		// TODO Auto-generated method stub
		
	}


}
