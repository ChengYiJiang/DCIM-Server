/**
 * 
 */
package com.raritan.tdz.item.validators;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.beanvalidation.home.BeanValidationHome;
import com.raritan.tdz.beanvalidation.home.BeanValidationHomeImpl;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemDomainAdaptor;
import com.raritan.tdz.item.home.ItemObject;
import com.raritan.tdz.item.home.ItemObjectFactory;
import com.raritan.tdz.item.itemState.ItemStateContext;
import com.raritan.tdz.util.UniqueValidator;

public class FreeStandingCabinetItemValidator extends ItemValidator {
	

	private ItemStateContext itemStateContext;

	@Autowired
	private ItemDomainAdaptor itemDomainAdaptor;
	
	public FreeStandingCabinetItemValidator(SessionFactory sessionFactory, BeanValidationHome beanValidationHome,
							ItemObjectFactory itemObjectFactory){
		super(sessionFactory, beanValidationHome, itemObjectFactory);
	}

	public ItemStateContext getItemStateContext() {
		return itemStateContext;
	}

	public void setItemStateContext(ItemStateContext itemStateContext) {
		this.itemStateContext = itemStateContext;
	}

	@Override
	protected void stateValidation(Object target, UserInfo sessionUser, Errors errors){
		itemStateContext.validateAllButReqFields(target, sessionUser, errors);	
	}
}
