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

import com.raritan.tdz.domain.ItItem;
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
public abstract class ItemSearchHelperME extends ItemSearchHelperTemplate {
	
	protected DtoToDomainObjectTrace meItemSearchResultDTOToDomainTrace;
	
	public ItemSearchHelperME(MessageSource messageSource,
			DozerBeanMapper dzMapper, Map<String, ItemSearchTypeRestriction> itemSearchTypeRestriction,
			DtoToDomainObjectTrace commonItemSearchDtoToDomainTrace) {
		super(messageSource, dzMapper, itemSearchTypeRestriction, commonItemSearchDtoToDomainTrace);
	}

	@Override
	protected Criteria createCriteria(Session session) {
		return session.createCriteria(MeItem.class);
	}

	@Override
	protected void addItemClassRestriction(Criteria searchCriteria,
			Long classLkpValueCode) {
		searchCriteria.add(Restrictions.eq("classLookup.lkpValueCode",classLkpValueCode.longValue()));
	}
	
	
	@Override
	protected ProjectionList getProjections() {
		return super.getProjections(meItemSearchResultDTOToDomainTrace);
	}
	
	@Override
	protected void addAlias(Criteria searchCriteria) {
		super.addAlias(meItemSearchResultDTOToDomainTrace, searchCriteria);
	}
}
