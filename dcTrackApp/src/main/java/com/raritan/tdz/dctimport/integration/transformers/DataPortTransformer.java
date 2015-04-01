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
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.session.dao.UserSessionDAO;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 * @author bunty
 *
 */
public class DataPortTransformer implements ImportTransformer {

	private String uuid;
	
	@Autowired
	private UserSessionDAO userSessionDAO;
	
	private BeanToDataPortDtoConverter beanToDataPortDtoConverter;
	
	private ImportErrorHandler importErrorHandler; 
	
	public DataPortTransformer(String uuid, BeanToDataPortDtoConverter beanToDataPortDtoConverter) {
		this.uuid = uuid;
		this.beanToDataPortDtoConverter = beanToDataPortDtoConverter;
	}
	
	public ImportErrorHandler getImportErrorHandler() {
		return importErrorHandler;
	}

	public void setImportErrorHandler(ImportErrorHandler importErrorHandler) {
		this.importErrorHandler = importErrorHandler;
	}

	public Message<?> transform(Message<?> message) throws Exception {
		
		DCTImport beanObj = (DCTImport)  message.getPayload();
		
		DataPortDTO dataPortDTO;
		try {

			Errors dataPortConvertErrors = new MapBindingResult(new HashMap<String, String>(), "dpConvertValidationErrors");
			
			dataPortDTO = beanToDataPortDtoConverter.convertBeanToDataPortDTO(beanObj, dataPortConvertErrors);
			
			if (dataPortConvertErrors.hasErrors()) {
				importErrorHandler.handleLineErrors(dataPortConvertErrors);
				
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
		Object[] newPayLoadArray = {dataPortDTO, userInfo};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();

	    return newMessage;
	}
	
}
