package com.raritan.tdz.dctimport.integration.transformers;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.integration.exceptions.IgnoreException;
import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.session.dao.UserSessionDAO;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 * @author bunty
 *
 */
public class PowerPortTransformer implements ImportTransformer {

	private String uuid;
	
	@Autowired
	private UserSessionDAO userSessionDAO;
	
	private BeanToPowerPortDtoConverter beanToPowerPortDtoConverter;
	
	private ImportErrorHandler importErrorHandler; 
	
	public PowerPortTransformer(String uuid, BeanToPowerPortDtoConverter beanToPowerPortDtoConverter) {
		this.uuid = uuid;
		this.beanToPowerPortDtoConverter = beanToPowerPortDtoConverter;
	}
	
	public ImportErrorHandler getImportErrorHandler() {
		return importErrorHandler;
	}

	public void setImportErrorHandler(ImportErrorHandler importErrorHandler) {
		this.importErrorHandler = importErrorHandler;
	}

	public Message<?> transform(Message<?> message) throws Exception {
		
		DCTImport beanObj = (DCTImport)  message.getPayload();
		
		PowerPortDTO powerPortDTO;
		try {

			Errors powerPortConvertErrors = new MapBindingResult(new HashMap<String, String>(), "ppConvertValidationErrors");
			
			powerPortDTO = beanToPowerPortDtoConverter.convertBeanToPowerPortDTO(beanObj, powerPortConvertErrors);
			
			if (powerPortConvertErrors.hasErrors()) {
				importErrorHandler.handleLineErrors(powerPortConvertErrors);
				
				throw new IgnoreException();
				
			}
		
		} catch (DataAccessException e1) {
			
			return message;
		} 

		UserInfo userInfo;
		try {
			userInfo = userSessionDAO.getUserInfo(uuid);
		} catch (DataAccessException e) {

			e.printStackTrace();
			throw new Exception(e);
		}
		Object[] newPayLoadArray = {powerPortDTO, userInfo};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();

	    return newMessage;
	}
	
}
