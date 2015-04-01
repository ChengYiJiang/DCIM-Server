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
import org.hibernate.criterion.Restrictions;
import org.springframework.context.MessageSource;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 *
 */
public class ItemSearchHelperAll extends ItemSearchHelperTemplate {

	public ItemSearchHelperAll(MessageSource messageSource,
			DozerBeanMapper dzMapper, Map<String, ItemSearchTypeRestriction> itemSearchTypeRestriction,
			DtoToDomainObjectTrace commonItemSearchDtoToDomainTrace) {
		super(messageSource, dzMapper, itemSearchTypeRestriction, commonItemSearchDtoToDomainTrace);
	}


	@Override
	protected Criteria createCriteria(Session session) {
		return session.createCriteria(Item.class);
	}

	@Override
	protected void addItemClassRestriction(Criteria searchCriteria,
			Long classLkpValueCode) {
		searchCriteria.add(Restrictions.in("classLookup.lkpValueCode",
				new Long[]{
				SystemLookup.Class.DEVICE,
				SystemLookup.Class.NETWORK,
				SystemLookup.Class.PROBE,
				SystemLookup.Class.DATA_PANEL,
				SystemLookup.Class.RACK_PDU,
				SystemLookup.Class.FLOOR_OUTLET,
				SystemLookup.Class.CABINET,
				SystemLookup.Class.CRAC,
				SystemLookup.Class.FLOOR_PDU,
				SystemLookup.Class.UPS
				}
		));
	}

	@Override
	protected ProjectionList getProjections() {
		return null;
	}

	@Override
	protected void addAlias(Criteria searchCriteria) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void addSearchResult(ItemSearchResultDTOImpl searchResult, HashMap item) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void applyFilters(Criteria criteria, String filterKey, String filterValue) {
		//This is to avoid "DEVICE PORT TEMPLATE ITEM" record from appearing
		//I am assuming that this is the correct filter to use!
		criteria.add(Restrictions.gt("itemId",new Long(-1)));	
		
		criteria.add(Restrictions.or(
				Restrictions.ne("classLookup.lkpValueCode",SystemLookup.Class.FLOOR_PDU),
				Restrictions.and(
						Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.FLOOR_PDU), 
						Restrictions.isNull("parentItem"))
						)
				);
		
		if (filterKey != null && filterKey.equals("chassisName")){
			criteria.add(Restrictions.or(Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.BLADE),
					Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.BLADE_SERVER)));
		}
	}

}
