/**
 * 
 */
package com.raritan.tdz.item.home.search;

import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.DtoToDomainObjectTrace;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dozer.DozerBeanMapper;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.interceptor.CustomNullsFirstOrderByInterceptor;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.util.ObjectTracer;



/**
 * @author prasanna
 *
 */
@Transactional
public class ItemSearchImpl implements ItemSearch {
	Logger log = Logger.getLogger(getClass());

	
	private SessionFactory sessionFactory;
	private MessageSource messageSource = null;
	private DozerBeanMapper dzMapper;
	private Map<String,ItemSearchHelper> helpers;
	private DtoToDomainObjectTrace compositeTrace;

	public DtoToDomainObjectTrace getCompositeTrace() {
		return compositeTrace;
	}

	public void setCompositeTrace(DtoToDomainObjectTrace compositeTrace) {
		this.compositeTrace = compositeTrace;
	}

	/**
	 * @return Map<Long,ItemSearchHelper> all ItemSearchHelpers
	 */
	public Map<String,ItemSearchHelper> getHelpers() {
		return helpers;
	}

	/**
	 * @param helpers a Map of classLkpValueCode and ItemSearchHelper
	 */
	public void setHelpers(Map<String,ItemSearchHelper> helpers) {
		this.helpers = helpers;
	}
	
	/**
	 * @return the messageSource
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * @param messageSource the messageSource to set
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @return the dzMapper
	 */
	public DozerBeanMapper getDzMapper() {
		return dzMapper;
	}

	/**
	 * @param dzMapper the dzMapper to set
	 */
	public void setDzMapper(DozerBeanMapper dzMapper) {
		this.dzMapper = dzMapper;
	}

	public ItemSearchImpl(){
		
	}
	public ItemSearchImpl(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.ItemSearch#search(com.raritan.tdz.item.dto.ItemSearchCriteriaDTO)
	 */
	@Override
	public List<ItemSearchResultDTO> search(ItemSearchCriteriaDTO itemCriteria)
			throws SystemException, BusinessValidationException {
		
		if (itemCriteria == null){
			throw new IllegalArgumentException("No criteria provided to search");
		}
		
		Session session = sessionFactory.openSession(); //new CustomNullsFirstOrderByInterceptor());
		Criteria criteria = getHibernateCriteria(itemCriteria, session);
		List<ItemSearchResultDTO> dtoList =  criteria.list();
		session.close();
		
		return dtoList;
	}
	
	@Override
	public Long getTotalItemCountForClass(Long classLkpValueCode) {
		return helpers.get(classLkpValueCode.toString()).getTotalItemCountForClass(classLkpValueCode, sessionFactory.getCurrentSession());
	}


	private Criteria getHibernateCriteria(ItemSearchCriteriaDTO itemCriteria,
			Session session) throws SystemException, BusinessValidationException{		
		return helpers.get(itemCriteria.getItemClassLkpValueCode().toString()).createItemSearchCriteria(itemCriteria, compositeTrace, session);
	}

	private List<ItemSearchResultDTO> adaptDomainToDTO(List list, Long classLkpValueCode){
		return helpers.get(classLkpValueCode.toString()).getItemSearchResultDTO(list);
	}

}
