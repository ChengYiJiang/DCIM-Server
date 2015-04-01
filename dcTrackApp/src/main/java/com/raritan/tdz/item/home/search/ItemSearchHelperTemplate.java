/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.context.MessageSource;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.DtoToDomainObjectTrace;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author prasanna
 * Please note that this is not the same as ItemSearchHelperAll. 
 * The criteria created by this method does not apply the classLkpValueCode or Hibernate Projections.
 * Extend this class to put those specifics
 * 
 */
public abstract class ItemSearchHelperTemplate implements ItemSearchHelper {
	
	protected MessageSource messageSource;
	protected DozerBeanMapper dzMapper;
	protected String alias;
	protected Map<String, ItemSearchTypeRestriction> itemSearchTypeRestriction;
	
	private DtoToDomainObjectTrace commonDtoToDomainTrace;
	
	
	public ItemSearchHelperTemplate(MessageSource messageSource, DozerBeanMapper dzMapper, Map<String, ItemSearchTypeRestriction> itemSearchTypeRestriction,
			DtoToDomainObjectTrace commonDtoToDomainTrace){
		this.messageSource = messageSource;
		this.dzMapper = dzMapper;
		this.itemSearchTypeRestriction = itemSearchTypeRestriction;
		this.commonDtoToDomainTrace = commonDtoToDomainTrace;
	}
	
	

	public DtoToDomainObjectTrace getTrace() {
		return commonDtoToDomainTrace;
	}



	public void setTrace(DtoToDomainObjectTrace trace) {
		this.commonDtoToDomainTrace = trace;
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.ItemSearchHelper#createItemSearchCriteria(com.raritan.tdz.item.dto.ItemSearchCriteriaDTO, org.hibernate.Session)
	 */
	@Override
	public Criteria createItemSearchCriteria(
			ItemSearchCriteriaDTO itemCriteria, DtoToDomainObjectTrace trace, Session session)
			throws BusinessValidationException, SystemException {
		if (itemCriteria.getFilter() == null){
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.missingFilter",
				null,
				null);
			e.addValidationError(message);
			throw e;
		}
		
		//Local copy of all criteria dto information.
		//TODO: Make it final.
		String filterKey = itemCriteria.getFilter().getKey();
		String filterValue = itemCriteria.getFilter().getValue();
		String operator = itemCriteria.getFilter().getOperation();
		Long classLkpValueCode = itemCriteria.getItemClassLkpValueCode();
		String sortField = itemCriteria.getSortField();
		Boolean sortDescending = itemCriteria.getSortDescending();
		Integer pageNumber = itemCriteria.getPageNumber();
		Integer maxLinesPerPage = itemCriteria.getMaxLinesPerPage();
		
		//Dont accept if the classLkpValueCode is zero (not provided)
		if (classLkpValueCode == null){
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.missingFilter",
				null,
				null);
			e.addValidationError(message);
			throw e;
		}
		
		//Dont accept if the filter key is not provided.
		if (filterKey == null || (filterKey.isEmpty())){
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.missingFilterKey",
				null,
				null);
			e.addValidationError(message);
			throw e;
		}
		
		//Dont accept if the filter value is not provided
		if (filterValue == null || (filterValue.isEmpty())){
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.missingFilterValue",
				null,
				null);
			e.addValidationError(message);
			throw e;
		}
		
		if (operator == null || (operator.isEmpty())){
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.missingOperator",
				null,
				null);
			e.addValidationError(message);
			throw e;
		}
		
		if (sortDescending == null){
			sortDescending = false;
		}
		
		String field = trace.getTrace(filterKey);
		//Dont accept if the key is invalid
		if (field == null){
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.searchKeyNotFound",
				null,
				null);
			e.addValidationError(message);
			throw e;
		}
		
		String sortingField = trace.getTrace(sortField);
		if (sortField == null || sortingField == null){
			sortingField = "itemName";
		}
		
	
				
		Criteria criteria = createCriteria(session);

		//Add alias
		alias = field.contains(".") ? field.substring(0,  field.lastIndexOf(".")) : null;
		
		String sortingAlias = sortingField.contains(".") ? trace.getTraceExcludeLeafNode(sortField) : null;
		if (sortingAlias != null){
			sortingField = sortingAlias.replace(".","_") + "." + trace.getLastNode(sortField);
		}
		
		addCommonAlias(criteria);
		addAlias(criteria);
		
		//Add the class lookup restriction
		addItemClassRestriction(criteria, classLkpValueCode);
		
		//Add the alias for search field
		String searchKey = field.contains(".") ? field.substring(field.lastIndexOf(".")) : filterKey;
		
		//Setup the projections
		ProjectionList commonProList = getCommonProjections();
		ProjectionList specificProList = getProjections();
		if (specificProList != null)
			commonProList.add(specificProList);
		
		criteria.setProjection(commonProList);
		criteria.setResultTransformer(Transformers.aliasToBean(ItemSearchResultDTOImpl.class));

		//Add search criteria restriction
		addRestrictionForOperator(criteria, searchKey, filterValue, operator);
		
		if (sortDescending){
			criteria.addOrder(Order.desc((sortingField)));
		} else {
			criteria.addOrder(Order.asc((sortingField)));
		}
		
		if (pageNumber != null && maxLinesPerPage != null && maxLinesPerPage > 0){
			criteria.setFirstResult((pageNumber - 1) * maxLinesPerPage);
			criteria.setMaxResults(maxLinesPerPage);
		}
	
		
		//Apply any additional filters
		applyCommonFilters(criteria, filterKey, filterValue);
		applyFilters(criteria, filterKey, filterValue);
		
		//Enable query caching???
		criteria.setCacheable(true);
		
		return criteria;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.ItemSearchHelper#getItemSearchResultDTO(java.util.List)
	 */
	@Override
	public List<ItemSearchResultDTO> getItemSearchResultDTO(List queryResults) {
		List<ItemSearchResultDTO> searchResults = new ArrayList<ItemSearchResultDTO>();
		
		List<HashMap> items = queryResults;
		
		for (HashMap item : items){
			ItemSearchResultDTOImpl searchResult = dzMapper.map(item, ItemSearchResultDTOImpl.class);
			searchResult.setItemName((String)item.get("_itemName"));
			searchResult.setItemAlias((String)item.get("_itemAlias"));
			searchResult.setRaritanAssetTag((String)item.get("_raritanAssetTag"));
			searchResult.setFreeDataPortCount((Integer)item.get("_freeDataPortCount"));
			searchResult.setFreePowerPortCount((Integer)item.get("_freePowerPortCount"));
			addSearchResult(searchResult, item);
			searchResults.add(searchResult);
		}
		
		return searchResults;
	}
	
	@Override
	public Long getTotalItemCountForClass(Long classLkpValueCode, Session session) {
		Criteria criteria = createCriteria(session);
		criteria.createAlias("classLookup", "classLookup");
		addItemClassRestriction(criteria, classLkpValueCode);
		criteria.createAlias("statusLookup", "statusLookup");
		applyCommonFilters(criteria, null, null);
		applyFilters(criteria, null, null);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}
	
	/**
	 * This template method gets the hibernate criteria object for a specific dcTrack Item class
	 * @param session TODO
	 * @return returns the Item class specific criteria
	 */
	protected abstract Criteria createCriteria(Session session);
	
	/**
	 * This template method adds the dcTrack item class specific lkpValue code restriction
	 * @param searchCriteria
	 * @param classLkpValueCode TODO
	 */
	protected abstract void addItemClassRestriction(Criteria searchCriteria, Long classLkpValueCode);
	
	/**
	 * This template method adds dcTrack item class specific projections
	 * @return
	 */
	protected abstract ProjectionList getProjections();
	
	/**
	 * This template method helps in adding further hibernate alias that are item specific.
	 * @param searchCriteria
	 */
	protected abstract void addAlias(Criteria searchCriteria);
	
	/**
	 * Any further manipulation of the search result may be done here.
	 * Currently this is not used
	 * @param searchResult
	 * @param item
	 */
	@Deprecated
	protected abstract void addSearchResult(ItemSearchResultDTOImpl searchResult, HashMap item);
	
	/**
	 * This will help in applying any further filters that are item class specific
	 * @param criteria
	 * @param filterKey
	 * @param filterValue
	 */
	protected abstract void applyFilters(Criteria criteria, String filterKey, String filterValue);
	

	
	protected ProjectionList getProjections(DtoToDomainObjectTrace itemSearchDtoToDomainTrace){
		ProjectionList proList = Projections.projectionList();
		for (Field dtoObject: ItemSearchResultDTOImpl.class.getDeclaredFields()){
			String dtoObjectName = dtoObject.getName();
			String alias = itemSearchDtoToDomainTrace.getTraceExcludeLeafNode(dtoObjectName);
			String property = itemSearchDtoToDomainTrace.getLastNode(dtoObjectName);
			String lastNode = itemSearchDtoToDomainTrace.getLastNode(dtoObjectName);
			
			if (alias != null && !alias.equals(property)){
				property = alias.replace(".", "_") + "." + lastNode;
			}
			
			String proAlias = dtoObjectName;
			
			if (alias != null && alias.equals(dtoObjectName)){
				proAlias = "_" + dtoObjectName;
			} 
			
			if (property != null)
				proList.add(Projections.alias(Projections.property(property),proAlias));
		}
		
		return proList;
	}
	
	protected void addAlias(DtoToDomainObjectTrace itemSearchDtoToDomainTrace, Criteria searchCriteria){
		for (String alias: itemSearchDtoToDomainTrace.getAllTraceExcludeLeafNode()){
			searchCriteria.createAlias(alias, alias.replace(".", "_"), Criteria.LEFT_JOIN);
		}
	}
	
	private void addCommonAlias(Criteria searchCriteria){		
		
		for (String alias: commonDtoToDomainTrace.getAllTraceExcludeLeafNode()){
			searchCriteria.createAlias(alias, alias.replace(".", "_"), Criteria.LEFT_JOIN);
		}
	}
	
	private ProjectionList getCommonProjections() {
		ProjectionList proList = Projections.projectionList();
		
		for (Field dtoObject: ItemSearchResultDTOImpl.class.getDeclaredFields()){
			String dtoObjectName = dtoObject.getName();
			String alias = commonDtoToDomainTrace.getTraceExcludeLeafNode(dtoObjectName);
			String property = commonDtoToDomainTrace.getLastNode(dtoObjectName);
			String lastNode = commonDtoToDomainTrace.getLastNode(dtoObjectName);
			
			if (alias != null && !alias.equals(property)){
				property = alias.replace(".", "_") + "." + lastNode;
			}
			
			String proAlias = dtoObjectName;
			
			if (alias != null && alias.equals(dtoObjectName)){
				proAlias = "_" + dtoObjectName;// To handle the ???
			}
			
			if (property != null && property.equals("itemServiceDetails.itemAdminUser")){
				property = alias.replace(".", "_") + "_" + lastNode + ".userId"; //TODO: change it to string buffer.
			}
			
			if (property != null)
				proList.add(Projections.alias(Projections.property(property),proAlias));
		}
			
		return proList;
	}
	
	private void applyCommonFilters(Criteria searchCriteria, String filterKey, String filterValue){
		List<Long> statusLks = new ArrayList<Long>();
		statusLks.add(SystemLookup.ItemStatus.ARCHIVED);
		statusLks.add(SystemLookup.ItemStatus.HIDDEN);		
		searchCriteria.add(Restrictions.not(Restrictions.in("statusLookup.lkpValueCode", statusLks)));
	}
	
	private void addRestrictionForOperator(Criteria criteria, String filterKey, String filterValue, String operator) throws SystemException, BusinessValidationException{
		//First get the type of key from ItemSearchResultDTO
		try {
			String key = filterKey.contains(".") ? filterKey.split("\\.")[1] : filterKey;
			
			if (itemSearchTypeRestriction.get(key) != null){ //These are for special kind where we need to see the key to handle it special:-) . For example lks and lku
				itemSearchTypeRestriction.get(key).addRestrictionForOperator(criteria, alias, filterKey, filterValue, operator);
			} else {
				Class<?> filterKeyType = ItemSearchResultDTOImpl.class.getDeclaredField(key).getType();
				
				//Handle case for all primitive and Long, Integer, etc.
				if (itemSearchTypeRestriction.get(filterKeyType.getName()) != null)
				{
					itemSearchTypeRestriction.get(filterKeyType.getName()).addRestrictionForOperator(criteria, alias, filterKey, filterValue, operator);
				} else { //Else something is totally wrong in the data sent by the client. We do not have appropriate convertion types in the table
					BusinessValidationException ex =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
					String message = messageSource.getMessage("itemSearch.incorrectDataType",
						new Object[] { filterValue, key },
						null);
					ex.addValidationError(message);
					throw ex;
				}
			}
			
		} catch (BusinessValidationException e){ 
			throw e;
		} catch (Throwable e){
			throw new SystemException(e);
		}
		
		
	}
}
