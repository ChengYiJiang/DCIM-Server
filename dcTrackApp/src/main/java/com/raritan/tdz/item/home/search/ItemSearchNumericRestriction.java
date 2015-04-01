/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.MessageSource;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author prasanna
 *
 */
public class ItemSearchNumericRestriction implements ItemSearchTypeRestriction{
	
	private static final Map<String,String> typeMap = 
			Collections.unmodifiableMap(new HashMap<String,String>() {{
				put("double","java.lang.Double");
				put("int","java.lang.Integer");
				put("long","java.lang.Long");
				put("java.lang.Double","java.lang.Double");
				put("java.lang.Integer","java.lang.Integer");
				put("java.lang.Long","java.lang.Long");
			}});
	
	private static final Map<String,String> operatorToRestrictionMap = 
			Collections.unmodifiableMap(new HashMap<String,String>() {{
				put("=","eq");
				put(">","gt");
				put("<","lt");
				put(">=","ge");
				put("<=","le");
				put("in","ilike");
			}});
	
	private MessageSource messageSource;
	

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void addRestrictionForOperator(Criteria criteria, String alias,
			String filterKey, String filterValue, String operator)
			throws SystemException, BusinessValidationException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException {
		String key = filterKey.contains(".") ? filterKey.split("\\.")[1] : filterKey;
		Class<?> filterKeyType = ItemSearchResultDTOImpl.class.getDeclaredField(key).getType();
		String className = typeMap.get(filterKeyType.getName());
		if (className == null){
			BusinessValidationException ex =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.incorrectDataType",
				new Object[] { filterValue, key },
				null);
			ex.addValidationError(message);
			throw ex;
		}
		Class<?> type = Class.forName(className);
		
		if (!filterValue.matches("(-)?(\\d){1,10}(\\.)?((\\d){1,10})?")){
			BusinessValidationException ex =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.incorrectValueNumeric",
				new Object[] { filterValue, key },
				null);
			ex.addValidationError(message);
			throw ex;
		}
		
		Object filterValueObject = type.getConstructor(Class.forName("java.lang.String")).newInstance(filterValue);
		
		addOperatorRestriction(criteria, filterKey, operator, filterValueObject, alias);
		
	}

	private void addOperatorRestriction(Criteria criteria, String filterKey,
			String operator, Object filterValueObject, String alias)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		String restrictionOp = operatorToRestrictionMap.get(operator);
		Method restrictionOpMethod = Restrictions.class.getMethod(restrictionOp, String.class, Object.class);
		
		if (alias != null)
			criteria.add((Criterion) restrictionOpMethod.invoke(null, alias.replace(".", "_") +  filterKey, filterValueObject));
		else
			criteria.add((Criterion) restrictionOpMethod.invoke(null, filterKey, filterValueObject));
	}

}
