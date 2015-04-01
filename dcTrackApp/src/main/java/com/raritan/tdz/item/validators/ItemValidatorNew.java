/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;

/**
 * @author prasanna
 *
 */
public class ItemValidatorNew implements Validator {
	
	private List<Validator> validators;
	
//	private BeanValidationHome beanValidationHome;
//	private SessionFactory sessionFactory;
//	private UniqueValidator uniqueValidator;
//	private ItemStateContext itemStateContext;
//	private ItemObjectFactory itemObjectFactory;
//	
//	
//	
//	@Autowired
//	private ItemDomainAdaptor itemDomainAdaptor;
//	
//	public ItemValidator(SessionFactory sessionFactory, BeanValidationHome beanValidationHome,
//							ItemObjectFactory itemObjectFactory){
//		this.sessionFactory = sessionFactory;
//		this.beanValidationHome = beanValidationHome;
//		this.itemObjectFactory = itemObjectFactory;
//	}
//
//	public BeanValidationHome getBeanValidationHome() {
//		return beanValidationHome;
//	}
//
//	public void setBeanValidationHome(BeanValidationHome beanValidationHome) {
//		this.beanValidationHome = beanValidationHome;
//	}
//
//	public SessionFactory getSessionFactory() {
//		return sessionFactory;
//	}
//
//	public void setSessionFactory(SessionFactory sessionFactory) {
//		this.sessionFactory = sessionFactory;
//	}
//
//	public UniqueValidator getUniqueValidator() {
//		return uniqueValidator;
//	}
//
//	public void setUniqueValidator(UniqueValidator uniqueValidator) {
//		this.uniqueValidator = uniqueValidator;
//	}
//	
//	
//
//	public ItemStateContext getItemStateContext() {
//		return itemStateContext;
//	}
//
//	public void setItemStateContext(ItemStateContext itemStateContext) {
//		this.itemStateContext = itemStateContext;
//	}
//
	public List<Validator> getValidators() {
		return validators;
	}

	public void setValidators(List<Validator> validators) {
		this.validators = validators;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		boolean supported =  Item.class.equals(clazz);
		if (validators != null){
			for (Validator validator:validators){
				if( validator instanceof SupportedPortClassValidator ){
					supported = supported || validator.supports(clazz);
					if( supported == true ) break;
				}
			}
		}
		return supported;
	}

//	protected void stateValidation(Object target, Errors errors){
//		//Validate based on state
//		itemStateContext.validate(target, errors);
//	}
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		Map<String, Object> targetMap = (Map<String,Object>)target;
//		
//		Item item = (Item) targetMap.get(errors.getObjectName());
//		UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
//		
//		errors.addAllErrors(itemDomainAdaptor.getValidationErrors());
//		
//		//Validate the bean
//		Errors beanValidationErrors = beanValidationHome.validate(item);
//		errors.addAllErrors(beanValidationErrors);
//		
//		//Validate based on state
//		stateValidation(item, errors);
//		itemStateContext.isTransitionPermittedForUser(item, userInfo, errors);
		
		//Validate any custom validations
		if (validators != null){
			for (Validator validator:validators){
				validator.validate(targetMap, errors);
			}
		}
	}

}
