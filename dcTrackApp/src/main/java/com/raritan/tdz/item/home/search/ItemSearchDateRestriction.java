/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
public class ItemSearchDateRestriction implements ItemSearchTypeRestriction{
	
	private static final Map<String,String> operatorToRestrictionMap = 
			Collections.unmodifiableMap(new HashMap<String,String>() {{
				put("<","lt");
				put(">=","ge");
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
		try{
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		
			Date filterValueObject = dateFormat.parse(filterValue);
			
			if (operator.equals("<=")){
				//Special stuff for <=
				addOperatorRestrictionLe(criteria, alias, filterKey, filterValue, filterValueObject);
			} else	if (operator.equals("=")){
				//Special stuff for = operator since client does not send time portion of the dateTime!
				addOperatorRestrictionEq(criteria, alias, filterKey, filterValue);
			} else if (operator.equals(">")){
				//Special stuff for >
				addOperatorRestrictionGt(criteria, alias, filterKey, filterValue);
			} else {
				addOperatorRestriction(criteria, filterKey, operator, filterValueObject, alias);
			}
		}  catch (ParseException e) {
			BusinessValidationException ex =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.ITEM_SEARCH_VALIDATION_FAILED.value(), this.getClass()));
			String message = messageSource.getMessage("itemSearch.incorrectDateFormat",
				new Object[] { filterValue },
				null);
			ex.addValidationError(message);
			throw ex;
		}
		
	}

	//This is specifically to handle == case
	private void addOperatorRestrictionEq(Criteria criteria, String alias,
			String filterKey, String filterValue) throws ParseException {
		//First get the Date range
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date startDateTime = dateTimeFormat.parse(filterValue + " 00:00:00");
		Date endDateTime = dateTimeFormat.parse(filterValue + " 23:59:59");
		
		//Then create restriction
		if (alias != null)
			criteria.add(Restrictions.between(alias.replace(".", "_") + filterKey, startDateTime, endDateTime));
		else
			criteria.add(Restrictions.between(filterKey, startDateTime, endDateTime));
	}
	
	//This is specifically to handle <= case
	private void addOperatorRestrictionLe(Criteria criteria, String alias,
			String filterKey, String filterValue, Date filterValueObject) throws ParseException {
		//First get the Date range
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date startDateTime = dateTimeFormat.parse(filterValue + " 00:00:00");
		Date endDateTime = dateTimeFormat.parse(filterValue + " 23:59:59");
		
		//Then create restriction
		if (alias != null){
			criteria.add(Restrictions.or(Restrictions.between(alias.replace(".", "_") + filterKey, startDateTime, endDateTime),
					Restrictions.lt(alias.replace(".", "_") + filterKey, filterValueObject)));
		}
		else{
			criteria.add(Restrictions.or(Restrictions.between(filterKey, startDateTime, endDateTime),
					Restrictions.lt(filterKey, filterValueObject)));
		}
		
	}
	
	//This is specifically to handle <= case
	private void addOperatorRestrictionGt(Criteria criteria, String alias,
			String filterKey, String filterValue) throws ParseException {
		//First get the Date range
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date filterValueObject = dateTimeFormat.parse(filterValue + " 23:59:59");
		
		//Then create restriction
		if (alias != null){
			criteria.add(Restrictions.gt(alias.replace(".", "_") + filterKey, filterValueObject));
		}
		else{
			criteria.add(Restrictions.gt(filterKey, filterValueObject));
		}
		
	}

	//This will create restriction based on the operator provided using Java reflection
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
