package com.raritan.tdz.dctimport.integration.transformers;

import java.util.HashMap;
import java.util.List;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.integration.exceptions.IgnoreException;
import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Transforms the data to the format that CircuitPDHome interface can understand 
 * @author bunty
 *
 */
public class ImportBeanTransformer implements ImportTransformer {

	private ImportBeanToParameter beanConverter;
	
	private ImportErrorHandler importErrorHandler; 
	
	public ImportBeanTransformer(ImportBeanToParameter beanConverter) {

		this.beanConverter = beanConverter;
	}
	
	public ImportErrorHandler getImportErrorHandler() {
		return importErrorHandler;
	}

	public void setImportErrorHandler(ImportErrorHandler importErrorHandler) {
		this.importErrorHandler = importErrorHandler;
	}

	@Override
	public Message<?> transform(Message<?> message) throws Exception {
		DCTImport beanObj = (DCTImport)  message.getPayload();
		
		Object[] parameters;
		try {

			Errors dataConvertErrors = new MapBindingResult(new HashMap<String, String>(), "dataConvertValidationErrors");
			
			parameters = beanConverter.convert(beanObj, dataConvertErrors);
			
			if (dataConvertErrors.hasErrors()) {
				
				importErrorHandler.handleLineErrors(dataConvertErrors);
				
				throw new IgnoreException();
				
			}
		
		} catch (ServiceLayerException e1) {
			
			return message;
		} 
		
		Object[] newPayLoadArray = parameters;
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();

	    return newMessage;
	    
	}

}
