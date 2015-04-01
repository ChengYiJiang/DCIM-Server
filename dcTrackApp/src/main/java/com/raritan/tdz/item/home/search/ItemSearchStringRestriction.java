/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.InvocationTargetException;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.MessageSource;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author prasanna
 *
 */
public class ItemSearchStringRestriction implements ItemSearchTypeRestriction {

	MessageSource messageSource;
	
	
	public MessageSource getMessageSource() {
		return messageSource;
	}


	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.search.ItemSearchTypeRestriction#addRestrictionForOperator(org.hibernate.Criteria, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void addRestrictionForOperator(Criteria criteria, String alias,
			String filterKey, String filterValue, String operator)
			throws SystemException, BusinessValidationException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException {
		String key = filterKey.contains(".") ? filterKey.split("\\.")[1] : filterKey;
		if (!operator.equals("in")){
			BusinessValidationException ex =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.incorrectOperatorForString",
				new Object[] { key },
				null);
			ex.addValidationError(message);
			throw ex;
		} else {
			if (filterValue.equals("*"))
				return;
			
			if (alias != null){
				criteria.add(Restrictions.ilike(alias.replace(".", "_") + filterKey, filterValue,MatchMode.ANYWHERE));
			}
			else{
				criteria.add(Restrictions.ilike(filterKey, filterValue,MatchMode.ANYWHERE));
			}
		}
	}

}
