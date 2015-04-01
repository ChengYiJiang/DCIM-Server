/**
 * 
 */
package com.raritan.tdz.dctimport.processors;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @author prasanna
 *
 */
public class ImportProcessorIntroductionInterceptor implements
		IntroductionInterceptor {

	private final Logger logger = Logger.getLogger("dctImport");
	private Errors errors  = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName() + "Error");
	private Errors warnings  = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName() + "Warning");

	private final ImportErrorHandler importErrorHandlerGateway;
	
	ImportProcessorIntroductionInterceptor(ImportErrorHandler importErrorHandlerGateway){
		this.importErrorHandlerGateway = importErrorHandlerGateway;
	}
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = null;
		errors  = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName() + "Error");
		warnings  = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName() + "Warning");
		
		try {
			result = invocation.proceed();
		} catch (BusinessValidationException be){
			//TODO: Handle BusinessValidation Exception here.
			handleBusinessValidationException(be);
			importErrorHandlerGateway.handleLineErrors(errors);
			importErrorHandlerGateway.handleLineWarnings(warnings);
		} catch (BusinessInformationException be) {
			handleBusinessInformationException(be);
			importErrorHandlerGateway.handleLineWarnings(warnings);
		} catch (Throwable t){
			//TODO: Handle throwables here
			errors.reject("Import.system.exception");
			importErrorHandlerGateway.handleLineErrors(errors);
			logger.fatal(t.getMessage());
			t.printStackTrace();
		}
		
		return result;
	}

	private void handleBusinessInformationException(
			BusinessInformationException be) {
		
		Collection<String> infoList = be.getInformations().isEmpty() ? be.getValidationInformation() : be.getInformations().values();
		
		for (String info: infoList) {
			Object[] args = {info};
			warnings.reject("Import.businessValidationException.warning",args,"Import business validation warning");
		}
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.aop.DynamicIntroductionAdvice#implementsInterface(java.lang.Class)
	 */
	@Override
	public boolean implementsInterface(Class<?> intf) {
		return true;
	}
	
	private void handleBusinessValidationException(BusinessValidationException be) {
		
		Collection<String> errorList = be.getErrors().isEmpty() ? be.getValidationErrors() : be.getErrors().values();
		
		for (String error:errorList) {
			Object[] args = {error};
			errors.reject("Import.businessValidationException.error",args,"Import business validation failure");
		}
		
		if (be.getWarnings().isEmpty()) {
			List<String> warningStrings = be.getValidationWarnings();
			for (String warning:warningStrings) {
				Object[] args = {warning};
				warnings.reject("Import.businessValidationException.warning",args,"Import business validation warning");
			}
		} else {
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			
			for (Map.Entry<String, BusinessValidationException.Warning> warningEntry:warningMap.entrySet()){
				Object[] args = {warningEntry.getValue().getWarningMessage()};
				warnings.reject("Import.businessValidationException.warning",args,"Import business validation warning");
			}
		}
	
	}

}
