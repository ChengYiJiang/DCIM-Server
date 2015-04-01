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
public class ItemSearchHelperAllInCabinet extends ItemSearchHelperTemplate {


	private ItemSearchHelperIT itemSearchHelperIT;
	
	public ItemSearchHelperIT getItemSearchHelperIT() {
		return itemSearchHelperIT;
	}

	public void setItemSearchHelperIT(ItemSearchHelperIT itemSearchHelperIT) {
		this.itemSearchHelperIT = itemSearchHelperIT;
	}

	public ItemSearchHelperAllInCabinet(MessageSource messageSource,
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
		//We have to include all the in cabinet items into the criteria
		searchCriteria.add(Restrictions.in("classLookup.lkpValueCode",
				new Long[]{
					SystemLookup.Class.DEVICE,
					SystemLookup.Class.NETWORK,
					SystemLookup.Class.PROBE,
					SystemLookup.Class.DATA_PANEL,
					SystemLookup.Class.RACK_PDU,
					SystemLookup.Class.FLOOR_OUTLET
					}
		));
	}

	@Override
	protected ProjectionList getProjections() {
		return itemSearchHelperIT.getProjections();
		
	}

	@Override
	protected void addAlias(Criteria searchCriteria) {
		itemSearchHelperIT.addAlias(searchCriteria);
	}

	@Override
	protected void addSearchResult(ItemSearchResultDTOImpl searchResult, HashMap item) {
//		Long subClassLookup = (Long) item.get("subclassLkpValueCode");
//		if (subClassLookup.equals(SystemLookup.SubClass.BLADE) || subClassLookup.equals(SystemLookup.SubClass.BLADE_SERVER)){
//			searchResult.setCabinetName(null);
//			searchResult.setSlotPosition((Long)item.get("_slotPosition"));
//			searchResult.setUPosition(0);
//		} else {
//			searchResult.setChassisName(null);
//			searchResult.setSlotPosition(0);
//			searchResult.setUPosition((Long)item.get("_uPosition"));
//		}
		
		itemSearchHelperIT.addSearchResult(searchResult, item);
	}

	@Override
	protected void applyFilters(Criteria criteria, String filterKey, String filterValue) {
		itemSearchHelperIT.applyFilters(criteria, filterKey, filterValue);
	}

}
