package com.raritan.tdz.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.beanvalidation.home.BeanValidationHome;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
/**
 * <p>Aspect/Interceptor to validate DTOs that define Bean Validation annotations ( JSR - 303 ).</p>
 * 
 * <p>This is called on every "service" call before the actual service call gets executed.</p>
 * <p>Please note that you need to have the following defined in order to provide you the validation errors
 * 	<ol>
 * 	<li> Include Bean Validation annotations in the DTO ( JSR - 303 ). User Hibernate Bean validation libraries. 
 * 			For example @NotBlank for strings</li>
 *  <li> Define all your service packages in the following package structure: com.raritan.tdz.&lt;any grouping name for your service&gt;.service.&lt;your service class file&gt;</li>
 *  <li> Include user viewable error text in the exceptions_en_US.properties in following format:</li>
 *  	<ol>
 *  		<li> &lt;Bean validation Annotation Name&gt;.&lt;full package name for your DTO class&gt;.&lt;Your DTO Class name&gt;.FieldName=&lt;user visible text&gt; </li>
 *  	</ol>
 *  <li> Make sure you process BusinessValidationException for displaying the actual message to the user </li>
 *  </ol>
 *  </p>
 *  <h1> Setup </h1>
 *  <p> All the setup of DTOBeanValidationInterceptor is done in the validators.xml spring configuration. The properties such as springValidator and messageSource
 *  are already set.</p>
 *  <p> A default processablePackage is also set (com.raritan.tdz). The idea is to process only those DTO/Objects that we are interested in.
 *  All others objects/DTOs that are not in the list will be filtered out. If you have any DTO/Objects in any other packages, you may include that in the spring config
 *  under the property processablePackages list.</p>
 *  <p> Note that all the DTOs/Objects are arguments to your service </p>
 * @author prasanna
 *
 */
@Aspect
public class DTOBeanValidationInterceptor {
	
	private Logger log = Logger.getLogger(this.getClass());
	private ArrayList<String> processablePackages; //These are the packages that will be looked for bean validation annotated classes.
	private BeanValidationHome beanValidationHome;

	public BeanValidationHome getBeanValidationHome() {
		return beanValidationHome;
	}

	public void setBeanValidationHome(BeanValidationHome beanValidationHome) {
		this.beanValidationHome = beanValidationHome;
	}

	private Validator springValidator;
	
	private MessageSource messageSource;
	
	public DTOBeanValidationInterceptor() {
		log.info("DTOBeanValidationInterceptor constructor called ");
	}
	
	public ArrayList<String> getProcessablePackages() {
		return processablePackages;
	}


	public void setProcessablePackages(ArrayList<String> packages) {
		this.processablePackages = packages;
	}


	public Validator getSpringValidator() {
		return springValidator;
	}

	public void setSpringValidator(Validator springValidator) {
		this.springValidator = springValidator;
	}


	public MessageSource getMessageSource() {
		return messageSource;
	}



	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}



	/**
	 * This Aspect Pointcut is called to validate any DTO that has bean validation annotations in it.
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Before("execution(* com.raritan.tdz.*.service.*.*(..))")
	public void validateBefore(JoinPoint joinPoint) throws Throwable {
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		
		for (Object arg: joinPoint.getArgs()){
			if (arg != null && isProcessablePackage(arg)){
				Errors errors = beanValidationHome.validate(arg);
				
				if (errors.hasErrors()){
					List<ObjectError> objectErrors = errors.getAllErrors();
					for (ObjectError error: objectErrors){
						String msg = messageSource.getMessage(error, Locale.getDefault());
						e.addValidationError(msg);
						e.addValidationError(error.getCode(), msg);
					}
				}
			}
		}
		
		//If we have validation errors for any of the DTO arguments, then throw that.
		if (e.getValidationErrors().size() > 0){
			throw e;
		}
	}
	
	//Process only those DTOs/Objects that we are interested in. Filter out others.
	private boolean isProcessablePackage(Object arg){
		for (String pkgName: processablePackages){
			if (arg.getClass() != null && arg.getClass().getPackage() != null 
						&& arg.getClass().getPackage().getName() != null && arg.getClass().getPackage().getName().contains(pkgName)){
				return true;
			}
		}
		
		return false;
	}
}
