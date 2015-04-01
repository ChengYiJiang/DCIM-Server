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

/**
 * @author prasanna
 *
 */
public class ItemValidator implements Validator {
	
	private BeanValidationHome beanValidationHome;
	private SessionFactory sessionFactory;
	private UniqueValidator uniqueValidator;
	private ItemStateContext itemStateContext;
	private ItemObjectFactory itemObjectFactory;
	
	@Autowired
	private ItemDomainAdaptor itemDomainAdaptor;
	
	public ItemValidator(SessionFactory sessionFactory, BeanValidationHome beanValidationHome,
							ItemObjectFactory itemObjectFactory){
		this.sessionFactory = sessionFactory;
		this.beanValidationHome = beanValidationHome;
		this.itemObjectFactory = itemObjectFactory;
	}

	public BeanValidationHome getBeanValidationHome() {
		return beanValidationHome;
	}

	public void setBeanValidationHome(BeanValidationHome beanValidationHome) {
		this.beanValidationHome = beanValidationHome;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public UniqueValidator getUniqueValidator() {
		return uniqueValidator;
	}

	public void setUniqueValidator(UniqueValidator uniqueValidator) {
		this.uniqueValidator = uniqueValidator;
	}
	
	

	public ItemStateContext getItemStateContext() {
		return itemStateContext;
	}

	public void setItemStateContext(ItemStateContext itemStateContext) {
		this.itemStateContext = itemStateContext;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Item.class.equals(clazz);
	}

	protected void stateValidation(Object target, UserInfo sessionUser, Errors errors){
		//Validate based on state
		itemStateContext.validate(target, errors);
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		Item item = (Item) target;
		
		errors.addAllErrors(itemDomainAdaptor.getValidationErrors());
		
		//Validate the bean
		Errors beanValidationErrors = beanValidationHome.validate(item);
		errors.addAllErrors(beanValidationErrors);
		
		//Validate the uniquness of the item.
		//validateItemNameUniqueness(errors, item);
		
		ItemObject itemObject = itemObjectFactory.getItemObject(item);
		if (itemObject != null){
			itemObject.validate(target, errors);
		}
		else{ //itemObject cannot be null. If null the model defines in the model library is not supported
			//CR 50915
			errors.reject("ItemConvert.invalidModelLibraryData");
		}
		
		//Validate based on state
		stateValidation(target, null, errors);
	}

}
